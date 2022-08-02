package com.example.appstore.download

import android.os.AsyncTask
import android.util.Log
import com.example.appstore.application.AppApplication
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile

/**
 * @Author : zhaojianwei02
 * @Date : 2022/8/1 6:28 下午
 * @Description : 构造方法中传入我们定义的接口，待会就可以把下载的结果通过这个参数进行回调
 */
class DownloaderTask(listen: DownloaderListener?) : AsyncTask<String, Int, Int>() {

    private val Tag = DownloaderTask::class.java.toString()

    //四个常量表示下载状态：分别为成功，失败，暂停，取消。
    val TYPE_SUCCESS = 0
    val TYPE_FAILED = 1
    val TYPE_PAUSED = 2
    val TYPE_CANCLED = 3

    private var listener: DownloaderListener? = listen
    private var isPaused = false
    private var isCancled = false
    private var lastProgress = 0

    /**
     * 后台任务开始执行之前调用，用于进行一些界面上的初始化操作，如显示进度条。
     */
    override fun onPreExecute() {
        super.onPreExecute()
    }

    /**
     * 后台任务：
     * 子线程中执行耗时操作。任务完成可以用return语句来返回任务的结果。
     * 如果需要更新UI，可以调用 publishProgress();
     *
     * @param params 这里的参数就是根据我们制指定的泛型来的
     * @return
     */
    override fun doInBackground(vararg params: String): Int? {
        var inputStream: InputStream? = null
        var savedFile: RandomAccessFile? = null //RandomAccessFile 是随机访问文件(包括读/写)的类
        var file: File? = null
        try {
            var downloadLength: Long = 0 //记录已下载的文件的长度(默认为0)
            val downloadUrl = params[0]
            //截取下载的URL的最后一个"/"后面的内容，作为下载文件的文件名
            val fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"))
            //将文件下载到sd卡的根目录下
//            val directory = Environment.getExternalStorageDirectory().absolutePath // 会报错：permission denied
            val directory = AppApplication.getContext()?.filesDir?.absolutePath
            file = File("$directory/$fileName")
            if (file.exists()) { //判断文件是否已经存在
                downloadLength = file.length() //如果文件已经存在，读取文件的字节数。（这样后面能开启断点续传）
            }
            val contentLength = getContentLength(downloadUrl) //获取待下载文件的总长度
            if (contentLength == 0L) {
                return TYPE_FAILED //待下载文件字节数为0，说明文件有问题，直接返回下载失败。
            } else if (downloadLength == contentLength) {
                return TYPE_SUCCESS //待下载文件字节数=已下载文件字节数，说明文件已经下载过。
            }
            val client = OkHttpClient()
            val request: Request = Request.Builder() //断点续传，指定从哪个文件开始下载
                .addHeader("RANGE", "bytes=$downloadLength-")
                .url(downloadUrl)
                .build()
            val response: Response = client.newCall(request).execute()
            if (response != null) { //返回数据不为空，则使用java文件流的方式，不断把数据写入到本地
                inputStream = response.body?.byteStream()
                savedFile = RandomAccessFile(file, "rw")
                savedFile.seek(downloadLength) //断点续传--跳过已经下载的字节
                var total = 0 //记录此次下载的字节数，方便计算下载进度
                val b = ByteArray(1024)
                var len: Int
                while (inputStream?.read(b).also { len = it!! } != -1) {
                    //下载是一个持续过程，用户随时可能暂停下载或取消下载
                    //所以把逻辑放在循环中，在整个下载过程中随时进行判断
                    if (isCancled) {
                        return TYPE_CANCLED
                    } else if (isPaused) {
                        return TYPE_PAUSED
                    } else {
                        total += len
                        savedFile.write(b, 0, len)
                        //计算已经下载到的百分比
                        val progress = ((total + downloadLength) * 100 / contentLength).toInt()
                        publishProgress(progress)
                    }
                }
                response.body?.close()
                return TYPE_SUCCESS
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                savedFile?.close()
                if (isCancled && file != null) {
                    file.delete() //如果已经取消，并且文件不为空，则删掉下载的文件
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return TYPE_FAILED
    }

    /**
     * 当在后台任务中调用了publishProgress()后，onProgressUpdate很快就会被执行。
     *
     * @param values 参数就是在后台任务中传过来的，这个方法中可以更新UI。
     */
    override fun onProgressUpdate(vararg values: Int?) {
        val progress = values[0]
        if (progress != null) {
            if (progress > lastProgress) {
                listener?.onProgress(progress)
                lastProgress = progress
            }
        }
    }

    /**
     * 当后台任务执行完毕并调用return返回时，这个方法很快会被调用。返回的数据会被作为参数传到这个方法中
     * 可根据返回数据更新UI。提醒任务结果，关闭进度条等。
     *
     * @param integer 异步任务返回下载结果
     */
    override fun onPostExecute(integer: Int?) {
        //把下载结果通过接口回调传出去
        Log.d(Tag, "下载结果：$integer")
        when (integer) {
            TYPE_SUCCESS -> listener?.onSuccess()
            TYPE_FAILED -> listener?.onFaild()
            TYPE_PAUSED -> listener?.onPaused()
            TYPE_CANCLED -> listener?.onCancled()
            else -> {}
        }
    }

    //暂停下载
    fun pausedDownload() {
        isPaused = true
    }

    //取消下载
    fun cancledDownload() {
        isCancled = true
    }

    /**
     * 获取待下载文件的字节数
     *
     * @param downloadUrl
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun getContentLength(downloadUrl: String): Long {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(downloadUrl)
            .build()
        val response: Response = client.newCall(request).execute()
        if (response.isSuccessful) {
            return (response.body?.contentLength() ?: response.body?.close()) as Long
        }
        return 0
    }
}
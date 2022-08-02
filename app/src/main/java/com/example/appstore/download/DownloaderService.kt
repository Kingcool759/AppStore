package com.example.appstore.download

import android.app.*
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.appstore.DownloadActivity
import com.example.appstore.R
import com.example.appstore.application.AppApplication
import com.example.appstore.download.Ext.showPercentNum
import java.io.File

/**
 * @Author : zhaojianwei02
 * @Date : 2022/8/1 6:25 下午
 * @Description :
 */
class DownloaderService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    private val mBinder = DownloadBinder()
    private var downLoadTask //要通过服务来下载，当然要在服务中创建下载任务并执行。
            : DownloaderTask? = null
    private var downloadUrl: String? = null

    //创建一个下载的监听
    private val listener: DownloaderListener = object : DownloaderListener {
        //通知进度
        override fun onProgress(progress: Int) {
            //下载过程中不停更新进度
            getNotificationManager().notify(1, getNotification("正在下载...", progress))
        }

        //下载成功
        override fun onSuccess() {
            downLoadTask = null
            //下载成功时将前台服务通知关闭，并创建一个下载成功的通知
            stopForeground(true)
            getNotificationManager().notify(1, getNotification("下载成功！", -1))

            // 下载成功后提示并删除文件重新下载，压测使用
            deleteFileAndReDownload()
        }

        //下载失败
        override fun onFaild() {
            downLoadTask = null
            //下载失败时将前台服务通知关闭，并创建一个下载成功的通知
            getNotificationManager().notify(1, getNotification("下载失败！", -1))
        }

        //暂停下载
        override fun onPaused() {
            downLoadTask = null
        }

        //取消下载
        override fun onCancled() {
            downLoadTask = null
            stopForeground(true)
        }
    }

    /**
     * 代理对象：在这里面添加三个方法：
     * 开始下载，暂停下载，取消下载
     * 就可以在Activity中绑定Service，并控制Service来实现下载功能
     */
    inner class DownloadBinder : Binder() {
        //开始下载，在Activity中提供下载的地址
        fun startDownload(url: String) {
            if (downLoadTask == null) {
                downLoadTask = DownloaderTask(listener)
                downloadUrl = url
                downLoadTask!!.execute(downloadUrl)
                startForeground(1, getNotification("正在下载...", 0)) //开启前台通知
            }
        }

        //暂停下载
        fun pausedDownload() {
            downLoadTask?.pausedDownload()
        }

        //取消下载
        fun cancledDownload() {
            if (downLoadTask != null) {
                downLoadTask!!.cancledDownload()
            } else {
                if (downloadUrl != null) {
                    //取消下载时需要将下载的文件删除  并将通知关闭
                    val fileName: String = downloadUrl!!.substring(downloadUrl!!.lastIndexOf("/"))
                    val directory =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).parent
                    val file = File(directory + fileName)
                    if (file.exists()) {
                        file.delete()
                    }
                    getNotificationManager().cancel(1)
                    stopForeground(true)
                }
            }
            showPercentNum("已取消")
        }
    }

    /**
     * 删除文件再次下载，人造循环，压测使用
     */
    private fun deleteFileAndReDownload() {
        Toast.makeText(AppApplication.getContext(), "下载已完成，将删除文件，进行重新下载……", Toast.LENGTH_LONG).show()
//        mBinder.cancledDownload()
//        mBinder.startDownload(DownloadActivity.url)
    }

    private fun getNotification(title: String, progress: Int): Notification? {
        val CHANNEL_ONE_ID = "com.primedu.cn"
        val CHANNEL_ONE_NAME = "Channel One"
        var notificationChannel: NotificationChannel? = null
        // sdk版本 > 26 时，需要加channelid否则报错：android.app.RemoteServiceException: Bad notification for startForeground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                CHANNEL_ONE_ID,
                CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.setShowBadge(true)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
        val intent = Intent(this, DownloadActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val builder = NotificationCompat.Builder(this)
        builder.setSmallIcon(R.drawable.huge)
        builder.setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.huge))
        builder.setContentIntent(pendingIntent)
        builder.setContentTitle(title)
        builder.setChannelId(CHANNEL_ONE_ID) // sdk版本 > 26 时，需要加channelid，否则报错
        if (progress >= 0) {
            builder.setContentText("$progress%")
//            WindowUtils.setPercentNum("$progress%")
            builder.setProgress(100, progress, false) //最大进度。当前进度。是否使用模糊进度
        }
        return builder.build()
    }

    //获取通知管理器
    private fun getNotificationManager(): NotificationManager {
        return getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }
}
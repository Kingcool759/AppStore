package com.example.appstore

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.appstore.download.DownloaderService
import org.koin.android.viewmodel.ext.android.viewModel

class DownloadActivity : AppCompatActivity(), View.OnClickListener {

    private val Tag = DownloadActivity::class.java.toString()

    private var downloadBinder: DownloaderService.DownloadBinder? = null

    // koin依赖注入
    val viewModel: DownloadViewModel by viewModel()

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            downloadBinder = service as DownloaderService.DownloadBinder
            Log.d(Tag, "## onServiceConnected ")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(Tag, "## onServiceDisconnected ")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btnStartDownload).setOnClickListener(this)
        findViewById<View>(R.id.btnPauseDownload).setOnClickListener(this)
        findViewById<View>(R.id.btnCancleDownload).setOnClickListener(this)

        checkPermissions()
        Log.d(Tag, "当前App信息= " + viewModel.getData().toString())
    }

    private fun checkPermissions() {
        var targetSdkVersion = 0
        val requestArray = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )
        val info: PackageInfo = this.packageManager.getPackageInfo(this.packageName,0)
        targetSdkVersion = info.applicationInfo.targetSdkVersion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Build.VERSION.SDK_INT是获取当前手机版本 Build.VERSION_CODES.M为6.0系统
            //如果系统>=6.0
            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                //第 1 步: 检查是否有相应的权限
                val isAllGranted: Boolean = checkPermissionAllGranted(requestArray)
                if (isAllGranted) {
                    Log.d(Tag,"所有权限已经授权！");
                    Toast.makeText(this, "所有权限已经授权", Toast.LENGTH_SHORT).show()
                    initService()
                    return
                }
                // 一次请求多个权限, 如果其他有权限是已经授予的将会自动忽略掉
                ActivityCompat.requestPermissions(
                    this,
                    requestArray, 1
                )
            }
        }
    }

    private fun checkPermissionAllGranted(permissions: Array<String>): Boolean {
        permissions.forEach {
            if(ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            var isAllGranted = true
            for (grant in grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false
                    break
                }
            }
            if (isAllGranted) {
                Log.d(Tag, "## permission 已获取到所有权限, 即将开启下载服务")
                initService()
            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                val intent = Intent()
                intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
        }
    }


    private fun initService() {
        val intent = Intent(this, DownloaderService::class.java)
        Log.d(Tag, "当前sdk版本为：" + Build.VERSION.SDK_INT)
        // 8.0以后需要加
        intent.component = ComponentName(this, "com.example.appstore.download.DownloaderService")
        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent);
//        } else {
//            startService(intent);//启动服务
//        }
        startService(intent) //启动服务
        bindService(intent, connection, BIND_AUTO_CREATE) //绑定服务
    }

    override fun onClick(v: View?) {
        if (downloadBinder == null) {
            Log.d(Tag, "downloadBinder = null")
            return
        }
        when (v?.id) {
            R.id.btnStartDownload -> viewModel.getData().downLoadUrl?.let {
                downloadBinder!!.startDownload(it)
            }
            R.id.btnPauseDownload -> downloadBinder!!.pausedDownload()
            R.id.btnCancleDownload -> {
                downloadBinder!!.cancledDownload()
            }
            else -> {}
        }
    }

}
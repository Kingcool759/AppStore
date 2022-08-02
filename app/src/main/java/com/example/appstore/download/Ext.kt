package com.example.appstore.download

import android.Manifest
import android.util.Log
import android.widget.Toast
import com.example.appstore.application.AppApplication
import com.permissionx.guolindev.PermissionX

/**
 * @Author : zhaojianwei02
 * @Date : 2022/8/1 7:09 下午
 * @Description :
 */
object Ext {
    fun showPercentNum(percentNum: String?) {
        Log.d(AppApplication.getContext().toString(), "当前下载进度为$percentNum")
    }

    fun getContext() {
        AppApplication.getContext()
    }
}
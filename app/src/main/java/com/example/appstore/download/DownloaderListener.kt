package com.example.appstore.download

/**
 * @Author : zhaojianwei02
 * @Date : 2022/8/1 6:24 下午
 * @Description : 下载状态接口
 */
interface DownloaderListener {
    fun onProgress(progress: Int) // 当前下载进度

    fun onSuccess() //下载成功

    fun onFaild() //下载失败

    fun onPaused() //暂停下载

    fun onCancled() //取消下载
}
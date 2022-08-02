package com.example.appstore

import androidx.lifecycle.ViewModel
import com.example.appstore.repository.AppRepository

/**
 * @Author : zhaojianwei02
 * @Date : 2022/8/1 6:14 下午
 * @Description : 数据请求
 */
class DownloadViewModel(private val repository: AppRepository): ViewModel() {
    fun getData() = repository.getAppData()
}
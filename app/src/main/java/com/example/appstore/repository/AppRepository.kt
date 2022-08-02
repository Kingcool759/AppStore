package com.example.appstore.repository

import com.example.appstore.bean.AppInfo

/**
 * @Author : zhaojianwei02
 * @Date : 2022/8/1 7:42 下午
 * @Description :
 */
interface AppRepository {
    fun getAppData(): AppInfo
}
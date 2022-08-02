package com.example.appstore.repository

import com.example.appstore.bean.AppInfo

/**
 * @Author : zhaojianwei02
 * @Date : 2022/8/1 7:43 下午
 * @Description :
 */
class AppRepositoryImpl: AppRepository {
    override fun getAppData(): AppInfo {
        return mockData()
    }

    private fun mockData(): AppInfo {
        val appInfo = AppInfo()
        appInfo.apply {
            this.appName = "王者荣耀"
            this.description = "5V5对战游戏"
            this.downLoadUrl = "http://app.mi.com/download/395801"
        }
        return appInfo
    }
}
package com.example.appstore.bean

/**
 * @Author : zhaojianwei02
 * @Date : 2022/8/1 6:10 下午
 * @Description : App信息
 */
class AppInfo {
    // 名字
    var appName: String? = null

    // 图标
    var icon: String? = null

    // 下载地址
    var downLoadUrl: String? = null

    // 软件的大小
    var size: String? = null

    // 软件的版本号
    var version: String? = null

    // 软件的包名
    var packageName: String? = null

    // 软件的更新时间
    var updateTime: String? = null

    // 软件的功能介绍
    var description: String? = null

    // 所需要的权限
    var permissionList: List<String>? = null

    //是否已安装
    var isInstall = false

    //是否需要升级
    var isUpdate = false

    override fun toString(): String {
        return "AppInfo(appName=$appName, icon=$icon, downLoadUrl=$downLoadUrl, size=$size, version=$version, packageName=$packageName, updateTime=$updateTime, description=$description, permissionList=$permissionList, isInstall=$isInstall, isUpdate=$isUpdate)"
    }


}
package com.example.appstore.bean

/**
 * @Author : zhaojianwei02
 * @Date : 2022/8/1 6:05 下午
 * @Description : 小米应用商店apk信息（仅供参考）
 */
class XiaoMiAppInfo {
    // 名字
    var appName: String? = null

    // 图片
    var icon: String? = null

    // 详情页面
    var detailUrl: String? = null

    // 下载地址
    var downLoadUrl: String? = null

    // 公司
    var company: String? = null

    // 星星的等级
    var star = 0.0

    // 软件的大小
    var size: String? = null

    // 软件的版本号
    var version: String? = null

    // 软件的包名
    var packageName: String? = null

    // 软件的更新时间
    var updateTime: String? = null

    // 软件的几张缩略图
    var imgs: List<String>? = null

    // 软件的功能介绍
    var description: String? = null

    // 软件的更新日志
    var updateLog: String? = null

    // 评分的人数
    var commentNum: String? = null

    // 软件的类别
    var category: String? = null

    // 所需要的权限
    var permissionList: List<String>? = null

    //同个开发者的应用
    var sameAppList: ArrayList<XiaoMiAppInfo>? = null

    //是否已安装
    var isInstall = false

    //是否需要升级
    var isUpdate = false
}
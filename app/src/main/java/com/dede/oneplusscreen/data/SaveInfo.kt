package com.dede.oneplusscreen.data

/**
 * @author hsh
 * @time 2017/7/10 010 下午 05:41.
 * @doc 读取存储的数据
 */
class SaveInfo {
    var sRbgState: Boolean = false//各个模式状态
    var dciP3State: Boolean = false
    var adobeRgbState: Boolean = false
    var electronicInkState: Boolean = false

    var rootState: Boolean = false//root状态
    var lcdInfo: String? = null//屏幕信息

    var sRgbEnabled: Boolean = false//可用状态
    var dciP3Enabled: Boolean = false
    var adobeRgbEnabled: Boolean = false

    var imagePath: String = ""//图片路径
    var imageState: Boolean = true//图片显示状态
    var iconState: Boolean = true//图标显示状态
}
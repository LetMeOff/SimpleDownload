package com.zjh.download.core

/**
 *  desc : 下载参数
 *
 *  自定义使用示例：
 *
 *  class CustomDownloadParam(url: String, saveName: String, savePath: String) : DownloadParam(url, saveName, savePath) {
 *       override fun tag(): String {
 *           // 使用文件路径作为唯一标示
 *           return savePath + saveName
 *       }
 *   }
 *
 *  @author zjh
 *  on 2021/8/24
 */
open class DownloadParam(
    var url: String,
    var saveName: String = "",
    var savePath: String = "",
) {

    /**
     * 默认使用url作为每个下载任务唯一标识，可重写此方法自定义
     */
    open fun tag() = url

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true

        return if (other is DownloadParam) {
            tag() == other.tag()
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return tag().hashCode()
    }

}
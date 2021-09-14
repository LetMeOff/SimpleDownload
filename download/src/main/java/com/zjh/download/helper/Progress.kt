package com.zjh.download.helper

import com.zjh.download.utils.formatSize
import com.zjh.download.utils.ratio

/**
 *  desc : 下载进度
 *  @author zjh
 *  on 2021/8/24
 */
class Progress(
    /**
     * 已下载大小
     */
    var downloadSize: Long = 0,
    /**
     * 总大小
     */
    var totalSize: Long = 0,
    /**
     * 用于标识一个链接是否是分块下载, 如果该值为true, 那么totalSize为-1
     */
    var isChunked: Boolean = false
) {
    /**
     * 返回总大小 如:10M
     */
    fun totalSizeStr(): String {
        return totalSize.formatSize()
    }

    /**
     * 返回已下载大小 如:3M
     */
    fun downloadSizeStr(): String {
        return downloadSize.formatSize()
    }

    /**
     * 返回百分比数字
     */
    fun percent(): Double {
        if (isChunked) return 0.0
        return downloadSize ratio totalSize
    }

    /**
     * 返回百分比字符串
     */
    fun percentStr(): String {
        return "${percent()}%"
    }
}
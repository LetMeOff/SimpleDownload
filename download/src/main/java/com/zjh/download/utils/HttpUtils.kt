package com.zjh.download.utils

import okhttp3.ResponseBody
import retrofit2.Response
import java.io.Closeable
import java.util.*
import java.util.regex.Pattern

/**
 *  desc : http相关
 *  @author zjh
 *  on 2021/8/24
 */

/**
 * 释放
 */
fun Closeable.closeQuietly() {
    try {
        close()
    } catch (rethrown: RuntimeException) {
        throw rethrown
    } catch (_: Exception) {
    }
}

fun Response<ResponseBody>.closeQuietly() {
    body()?.closeQuietly()
    errorBody()?.closeQuietly()
}

/**
 * 请求url
 */
fun Response<*>.url(): String {
    return raw().request.url.toString()
}

/**
 * 文件名
 */
fun Response<*>.fileName(): String {
    val url = url()

    var fileName = contentDisposition()
    if (fileName.isEmpty()) {
        fileName = getFileNameFromUrl(url)
    }

    return fileName
}

private fun Response<*>.contentDisposition(): String {
    val contentDisposition = header("Content-Disposition").lowercase(Locale.getDefault())

    if (contentDisposition.isEmpty()) {
        return ""
    }

    val matcher = Pattern.compile(".*filename=(.*)").matcher(contentDisposition)
    if (!matcher.find()) {
        return ""
    }

    var result = matcher.group(1)
    if (result.startsWith("\"")) {
        result = result.substring(1)
    }
    if (result.endsWith("\"")) {
        result = result.substring(0, result.length - 1)
    }

    result = result.replace("/", "_", false)

    return result
}

/**
 * 从url中获取文件名
 */
fun getFileNameFromUrl(url: String): String {
    var temp = url
    if (temp.isNotEmpty()) {
        val fragment = temp.lastIndexOf('#')
        if (fragment > 0) {
            temp = temp.substring(0, fragment)
        }

        val query = temp.lastIndexOf('?')
        if (query > 0) {
            temp = temp.substring(0, query)
        }

        val filenamePos = temp.lastIndexOf('/')
        val filename = if (0 <= filenamePos) temp.substring(filenamePos + 1) else temp

        if (filename.isNotEmpty() && Pattern.matches("[a-zA-Z_0-9.\\-()%]+", filename)) {
            return filename
        }
    }

    return ""
}

/**
 * 是否支持分片下载
 */
fun Response<*>.isSupportRange(): Boolean {
    if (code() == 206
        || header("Content-Range").isNotEmpty()
        || header("Accept-Ranges") == "bytes"
    ) {
        return true
    }
    return false
}

/**
 * contentLength
 */
fun Response<*>.contentLength(): Long {
    return header("Content-Length").toLongOrDefault(-1)
}

/**
 * 计算分片
 */
fun Response<*>.calcRanges(rangeSize: Long): Long {
    val totalSize = contentLength()
    val remainder = totalSize % rangeSize
    val result = totalSize / rangeSize

    return if (remainder == 0L) {
        result
    } else {
        result + 1
    }
}

/**
 * 获取header
 */
private fun Response<*>.header(key: String): String {
    val header = headers()[key]
    return header ?: ""
}

fun Response<*>.isChunked(): Boolean {
    return header("Transfer-Encoding") == "chunked"
}
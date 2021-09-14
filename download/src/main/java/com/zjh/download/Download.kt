package com.zjh.download

import com.zjh.download.core.DownloadParam
import com.zjh.download.core.DownloadTask
import com.zjh.download.core.DownloadConfig
import com.zjh.download.helper.Default
import com.zjh.download.utils.logD
import kotlinx.coroutines.CoroutineScope


/**
 * 扩展下载方法
 */
fun CoroutineScope.download(
    url: String,
    saveName: String = "",
    savePath: String = Default.DEFAULT_SAVE_PATH,
    downloadConfig: DownloadConfig = DownloadConfig()
): DownloadTask {
    logD("saveName : $saveName , path : $savePath")
    val downloadParams = DownloadParam(url, saveName, savePath)
    val task = DownloadTask(this, downloadParams, downloadConfig)
    return downloadConfig.taskManager.add(task)
}

/**
 * 扩展下载方法(自定义下载参数)
 */
fun CoroutineScope.download(
    downloadParam: DownloadParam,
    downloadConfig: DownloadConfig = DownloadConfig()
): DownloadTask {
    val task = DownloadTask(this, downloadParam, downloadConfig)
    return downloadConfig.taskManager.add(task)
}
package com.zjh.download

import kotlinx.coroutines.CoroutineScope

/**
 *  desc : 下载调用方法
 *  @author zjh
 *  on 2021/9/14
 */


///**
// * 扩展下载方法
// */
//fun CoroutineScope.download(
//    url: String,
//    saveName: String = "",
//    savePath: String = Default.DEFAULT_SAVE_PATH,
//    downloadConfig: DownloadConfig = DownloadConfig()
//): DownloadTask {
//    KLog.e(TAG,"saveName : $saveName , path : $savePath")
//    val downloadParams = DownloadParam(url, saveName, savePath)
//    val task = DownloadTask(this, downloadParams, downloadConfig)
//    return downloadConfig.taskManager.add(task)
//}
//
///**
// * 扩展下载方法(自定义下载参数)
// */
//fun CoroutineScope.download(
//    downloadParam: DownloadParam,
//    downloadConfig: DownloadConfig = DownloadConfig()
//): DownloadTask {
//    val task = DownloadTask(this, downloadParam, downloadConfig)
//    return downloadConfig.taskManager.add(task)
//}

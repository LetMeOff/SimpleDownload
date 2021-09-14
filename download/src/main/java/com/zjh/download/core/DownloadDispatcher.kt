package com.zjh.download.core

import com.zjh.download.utils.isSupportRange
import okhttp3.ResponseBody
import retrofit2.Response

/**
 *  desc : 下载器分发
 *  @author zjh
 *  on 2021/8/24
 */

interface DownloadDispatcher{
    fun dispatch(downloadTask: DownloadTask, resp: Response<ResponseBody>): Downloader
}

object DefaultDownloadDispatcher : DownloadDispatcher {
    override fun dispatch(downloadTask: DownloadTask, resp: Response<ResponseBody>): Downloader {
        return if (resp.isSupportRange()) {
            RangeDownloader(downloadTask.coroutineScope)
        } else {
            NormalDownloader(downloadTask.coroutineScope)
        }
    }
}
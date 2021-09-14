package com.zjh.download.core

import com.zjh.download.helper.DefaultHttpClientFactory
import com.zjh.download.helper.HttpClientFactory
import com.zjh.download.helper.apiCreator
import com.zjh.download.helper.Default
import okhttp3.ResponseBody
import retrofit2.Response

/**
 *  desc : 下载配置
 *  @author zjh
 *  on 2021/8/24
 */
class DownloadConfig(
    /**
     *下载管理
     */
    var taskManager: TaskManager = DefaultTaskManager,
    /**
     * 下载队列
     */
    var queue: DownloadQueue = DefaultDownloadQueue.get(),

    /**
     * 自定义header
     */
    var customHeader: Map<String, String> = emptyMap(),

    /**
     * 下载器分发
     */
    var dispatcher: DownloadDispatcher = DefaultDownloadDispatcher,

    /**
     * 分片下载每片的大小
     */
    var rangeSize: Long = Default.DEFAULT_RANGE_SIZE,

    /**
     * 分片下载并行数量
     */
    var rangeCurrency: Int = Default.DEFAULT_RANGE_CURRENCY,

    /**
     * 文件校验
     */
    var validator: FileValidator = DefaultFileValidator,

    /**
     * http client
     */
    var httpClientFactory: HttpClientFactory = DefaultHttpClientFactory,

    /**
     * http base url
     */
    var baseUrl: String = "http://www.example.com"
) {

    private val api = apiCreator(httpClientFactory.create(), baseUrl)

    /**
     * download
     */
    suspend fun request(url: String, header: Map<String, String>): Response<ResponseBody> {
        val tempHeader = mutableMapOf<String, String>().also {
            it.putAll(customHeader)
            it.putAll(header)
        }
        return api.get(url, tempHeader)
    }

}
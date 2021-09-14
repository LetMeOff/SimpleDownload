package com.zjh.download.core

import com.zjh.download.helper.Progress
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File

/**
 * CompletableDeferred<Progress> 携带回复的请求
 */
class QueryProgress(val completableDeferred: CompletableDeferred<Progress>)

interface Downloader {
    var actor: SendChannel<QueryProgress>

    suspend fun queryProgress(): Progress

    suspend fun download(
        downloadParam: DownloadParam,
        downloadConfig: DownloadConfig,
        response: Response<ResponseBody>
    )
}

@OptIn(ObsoleteCoroutinesApi::class)
abstract class BaseDownloader(protected val coroutineScope: CoroutineScope) : Downloader {
    protected var totalSize: Long = 0L
    protected var downloadSize: Long = 0L
    protected var isChunked: Boolean = false

    private val progress = Progress()

    /**
     * 定义一个sendChannel -> actor，用于发送请求，并接收一个 ReceiveChannel 回调
     */
    override var actor =
        CoroutineScope(Dispatchers.IO + Job()).actor<QueryProgress>(Dispatchers.IO) {
            //接收消息的迭代器 循环查询所有下载任务
            for (each in channel) {
                //接收一个携带回复的请求
                each.completableDeferred.complete(progress.also {
                    it.downloadSize = downloadSize
                    it.totalSize = totalSize
                    it.isChunked = isChunked
                })
            }
        }

    override suspend fun queryProgress(): Progress {
        //发送一个请求 查询下载进度
        val ack = CompletableDeferred<Progress>()
        val queryProgress = QueryProgress(ack)
        actor.send(queryProgress)
        return ack.await()
    }

    fun DownloadParam.dir(): File {
        return File(savePath)
    }

    fun DownloadParam.file(): File {
        return File(savePath, saveName)
    }
}
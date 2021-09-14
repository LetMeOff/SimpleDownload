package com.zjh.download.core

import com.zjh.download.helper.Progress
import com.zjh.download.helper.State
import com.zjh.download.helper.StateHolder
import com.zjh.download.helper.Default
import com.zjh.download.utils.clear
import com.zjh.download.utils.closeQuietly
import com.zjh.download.utils.fileName
import com.zjh.download.utils.logD
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File

/**
 *  desc : 下载任务
 *  @author zjh
 *  on 2021/8/24
 */
@OptIn(ObsoleteCoroutinesApi::class, FlowPreview::class, ExperimentalCoroutinesApi::class)
class DownloadTask(
    val coroutineScope: CoroutineScope,
    val param: DownloadParam,
    val config: DownloadConfig
) {

    /**
     * 下载状态
     */
    private val stateHolder by lazy { StateHolder() }

    /**
     * 下载任务
     */
    private var downloadJob: Job? = null
    private var downloader: Downloader? = null

    /**
     * 校验下载任务是否正在执行
     */
    private fun checkJob() = downloadJob?.isActive == true

    /**
     * 下载状态
     */
    private val downloadStateFlow = MutableStateFlow<State>(stateHolder.none)

    /**
     * 下载进度
     */
    private val downloadProgressFlow = MutableStateFlow(0)

    fun isStarted(): Boolean {
        return stateHolder.isStarted()
    }

    fun isFailed(): Boolean {
        return stateHolder.isFailed()
    }

    fun isSucceed(): Boolean {
        return stateHolder.isSucceed()
    }

    fun canStart(): Boolean {
        return stateHolder.canStart()
    }

    /**
     * 开始下载，添加到下载队列
     */
    fun start() {
        coroutineScope.launch {
            if (checkJob()) {
                return@launch
            }
            //修改状态
            notifyWaiting()
            try {
                //入队
                config.queue.enqueue(this@DownloadTask)
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    notifyFailed()
                }
                logD(e)
            }
        }
    }

    /**
     * 开始下载并等待下载完成，直接开始下载，不添加到下载队列
     */
    suspend fun suspendStart() {
        if (checkJob()) {
            return
        }

        downloadJob?.cancel()
        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            logD(throwable.toString())
            if (throwable !is CancellationException) {
                coroutineScope.launch {
                    notifyFailed()
                }
            }
        }
        downloadJob = coroutineScope.launch(errorHandler + Dispatchers.IO) {
            val response = config.request(param.url, Default.RANGE_CHECK_HEADER)
            try {
                if (!response.isSuccessful || response.body() == null) {
                    throw RuntimeException("request failed")
                }

                if (param.saveName.isEmpty()) {
                    //文件名
                    param.saveName = response.fileName()
                }
                if (param.savePath.isEmpty()) {
                    //保存路径
                    param.savePath = Default.DEFAULT_SAVE_PATH
                }
                if (downloader == null) {
                    downloader = config.dispatcher.dispatch(this@DownloadTask, response)
                }

                notifyStarted()

                val deferred =
                    async(Dispatchers.IO) { downloader?.download(param, config, response) }
                deferred.await()

                notifySucceed()
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    notifyFailed()
                }
                logD(e.message.toString())
            } finally {
                response.closeQuietly()
            }
        }
        downloadJob?.join()
    }

    /**
     * 停止下载
     */
    fun stop() {
        coroutineScope.launch {
            if (isStarted()) {
                config.queue.dequeue(this@DownloadTask)
                downloadJob?.cancel()
                notifyStopped()
            }
        }
    }

    /**
     * 移除任务
     */
    fun remove(deleteFile: Boolean = true) {
        stop()
        config.taskManager.remove(this)
        if (deleteFile) {
            file()?.clear()
        }
    }

    /**
     * @param interval 更新进度间隔时间，单位ms
     */
    fun state(interval: Long = 200): Flow<State> {
        return downloadStateFlow.combine(
            progress(
                interval,
                ensureLast = false
            )
        ) { l, r -> l.apply { progress = r } }
    }

    /**
     * @param interval 更新进度间隔时间，单位ms
     * @param ensureLast 能否收到最后一个进度
     */
    fun progress(interval: Long = 200, ensureLast: Boolean = true): Flow<Progress> {
        return downloadProgressFlow.flatMapConcat {
            //确保只发送一次
            var hasSend = false
            channelFlow {
                while (currentCoroutineContext().isActive) {
                    val progress = getProgress()

                    if (hasSend && stateHolder.isEnd()) {
                        if (!ensureLast) {
                            break
                        }
                    }

                    send(progress)
                    hasSend = true

                    if (progress.isComplete()) break

                    delay(interval)
                }
            }
        }
    }

    /**
     * 获取进度
     */
    suspend fun getProgress(): Progress {
        return downloader?.queryProgress() ?: Progress()
    }

    /**
     * 获取下载文件
     */
    fun file(): File? {
        return if (param.saveName.isNotEmpty() && param.savePath.isNotEmpty()) {
            File(param.savePath, param.saveName)
        } else {
            null
        }
    }

    /**
     * 改变下载状态为等待
     */
    private suspend fun notifyWaiting() {
        stateHolder.updateState(stateHolder.waiting, getProgress())
        downloadStateFlow.value = stateHolder.currentState
        logD("url ${param.url} download task waiting.")
    }

    /**
     * 改变下载状态为失败
     */
    private suspend fun notifyFailed() {
        stateHolder.updateState(stateHolder.failed, getProgress())
        downloadStateFlow.value = stateHolder.currentState
        logD("url ${param.url} download task failed.")
    }

    /**
     * 改变下载状态为开始
     */
    private suspend fun notifyStarted() {
        stateHolder.updateState(stateHolder.downloading, getProgress())
        downloadStateFlow.value = stateHolder.currentState
        downloadProgressFlow.value = downloadProgressFlow.value + 1
        logD("url ${param.url} download task start.")
    }

    /**
     * 改变下载状态为成功
     */
    private suspend fun notifySucceed() {
        stateHolder.updateState(stateHolder.succeed, getProgress())
        downloadStateFlow.value = stateHolder.currentState
        logD("url ${param.url} download task succeed.")
    }

    /**
     * 改变下载状态为停止
     */
    private suspend fun notifyStopped() {
        stateHolder.updateState(stateHolder.stopped, getProgress())
        downloadStateFlow.value = stateHolder.currentState
        logD("url ${param.url} download task stopped.")
    }

    private fun Progress.isComplete(): Boolean {
        return totalSize > 0 && totalSize == downloadSize
    }

    fun getState() = stateHolder.currentState

}
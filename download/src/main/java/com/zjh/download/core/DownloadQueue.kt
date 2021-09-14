package com.zjh.download.core

import com.zjh.download.helper.Default
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 *  desc : 下载队列
 *  @author zjh
 *  on 2021/8/24
 */
interface DownloadQueue {
    /**
     * 入队
     */
    suspend fun enqueue(task: DownloadTask)

    /**
     * 出队
     */
    suspend fun dequeue(task: DownloadTask)
}

/**
 * 默认下载队列
 */
class DefaultDownloadQueue private constructor(private val maxTask: Int) : DownloadQueue {

    /**
     * 通道处理协程通信
     */
    private val channel = Channel<DownloadTask>()

    /**
     * 任务map
     */
    private val tempMap = ConcurrentHashMap<String, DownloadTask>()

    init {
        CoroutineScope(Dispatchers.IO + Job()).launch {
            //循环接收
            repeat(maxTask) {
                launch {
                    channel.consumeEach {
                        //接收到任务开始下载 移除任务
                        if (tempMap[it.param.tag()] != null) {
                            it.suspendStart()
                            dequeue(it)
                        }
                    }
                }
            }
        }
    }

    override suspend fun enqueue(task: DownloadTask) {
        //加入map 并发送通道数据
        tempMap[task.param.tag()] = task
        channel.send(task)
    }

    override suspend fun dequeue(task: DownloadTask) {
        tempMap.remove(task.param.tag())
    }

    companion object {
        private val lock = Any()

        @Volatile
        private var instance: DefaultDownloadQueue? = null

        /**
         * 单例
         */
        fun get(maxTask: Int = Default.MAX_TASK_NUMBER): DefaultDownloadQueue =
            instance ?: synchronized(lock) {
                instance ?: DefaultDownloadQueue(maxTask).also { instance = it }
            }

    }

}

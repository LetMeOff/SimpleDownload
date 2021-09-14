package com.zjh.download.core

import java.util.concurrent.ConcurrentHashMap

/**
 * 任务管理
 */
interface TaskManager {
    fun add(task: DownloadTask): DownloadTask

    fun remove(task: DownloadTask)
}

/**
 * 任务管理实现类
 */
object DefaultTaskManager : TaskManager {
    /**
     * 任务列表
     */
    private val taskMap = ConcurrentHashMap<String, DownloadTask>()

    /**
     * 添加任务
     */
    override fun add(task: DownloadTask): DownloadTask {
        if (taskMap[task.param.tag()] == null) {
            taskMap[task.param.tag()] = task
        }
        return taskMap[task.param.tag()]!!
    }

    /**
     * 移除任务
     */
    override fun remove(task: DownloadTask) {
        taskMap.remove(task.param.tag())
    }
}
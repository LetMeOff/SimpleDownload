package com.zjh.download

import android.content.Context

/**
 *  desc : 初始化
 *  @author zjh
 *  on 2021/9/14
 */
class SimpleDownload private constructor() {

    lateinit var context: Context

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    companion object {
        val instance by lazy {
            SimpleDownload()
        }
    }
}



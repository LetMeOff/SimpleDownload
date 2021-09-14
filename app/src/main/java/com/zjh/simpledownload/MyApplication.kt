package com.zjh.simpledownload

import android.app.Application
import com.zjh.download.SimpleDownload

/**
 *  desc :
 *  @author zjh
 *  on 2021/9/14
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        SimpleDownload.instance.init(this)
    }
}
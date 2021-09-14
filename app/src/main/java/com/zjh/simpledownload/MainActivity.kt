package com.zjh.simpledownload

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zjh.download.SimpleDownload
import com.zjh.download.utils.download
import com.zjh.download.helper.State
import com.zjh.download.utils.logD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {

    private val nameLit = listOf("新浪微博", "腾讯手机管家", "腾讯浏览器")

    private val urlList = listOf(
        "https://imtt.dd.qq.com/16891/apk/96881CC7639E84F35E86421691CBBA5D.apk?fsname=com.sina.weibo_11.1.3_4842.apk&csr=3554",
        "https://imtt.dd.qq.com/16891/apk/DE071539CCD23453643F24779B052788.apk?fsname=com.tencent.qqpimsecure_8.10.0_1417.apk&csr=3554",
        "https://imtt.dd.qq.com/16891/apk/0F9F42DB8B17D9BA27A60D8D556F936C.apk?fsname=com.tencent.mtt_11.2.1.1506_11211500.apk&csr=3554"
    )

    private val scope by lazy {
        CoroutineScope(Dispatchers.IO + Job())
    }

    private val savePath by lazy {
        SimpleDownload.instance.context.filesDir.path + "/apks"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        urlList.forEachIndexed { index, s ->
            download(s, nameLit[index])
        }
    }

    private fun download(downloadUrl: String, saveName: String) {
        //创建下载任务
//        val downloadTask = lifecycleScope.download(downloadUrl)

        val downloadTask = scope.download(downloadUrl)

//        val downloadTask = scope.download(downloadUrl, saveName, savePath)

        //自定义参数
//        val params = DownloadParam(downloadUrl, saveName, savePath)
//        val config = DownloadConfig()
//        config.baseUrl = "http://www.example.com"
//        val downloadTask = scope.download(params,config)

        //状态监听
        downloadTask.state().onEach {
//            when {
//                downloadTask.isStarted() -> logD("正在下载中")
//                downloadTask.isFailed() -> logD("下载失败")
//                downloadTask.isSucceed() -> logD("下载成功")
//            }
            when (it) {
                is State.None -> logD("未开始任务")
                is State.Waiting -> logD("等待中")
                is State.Downloading -> logD("下载中")
                is State.Failed -> logD("下载失败")
                is State.Stopped -> logD("下载已暂停")
                is State.Succeed -> logD("下载成功")
            }
            logD("state : $it")
        }.launchIn(scope)

        //进度监听
        downloadTask.progress().onEach {
            logD("name : $saveName , progress : ${it.percentStr()}")
        }.launchIn(scope)

        //开始下载任务
        downloadTask.start()
    }
}
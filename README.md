# SimpleDownload

kotlin协程+channel实现下载，支持多任务下载、断点续传

## 添加依赖库

- 项目build.gradle

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

- 添加依赖

```
dependencies {
    implementation 'com.github.LetMeOff:SimpleDownload:v1.0.0'
}
```

## 使用

1. Application中初始化

```
SimpleDownload.instance.init(this)
```

2. 简单使用

```kotlin
//创建下载任务
val downloadTask = scope.download(downloadUrl)

//状态监听
downloadTask.state().onEach {
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
```

## 创建任务

- 可以在指定的协程中调用***download***方法开始任务，任务的生命周期取决于协程的生命周期。如：

```kotlin
val downloadTask = lifecycleScope.download(downloadUrl)
```

此下载任务会在***Activity***销毁后结束

- 默认保存名会从下载链接中获取，默认保存路径为```/data/data/包名/files```下，可以自定义

```kotlin
val downloadTask = scope.download(downloadUrl, saveName, savePath)
```

或者自定义参数

```kotlin
//自定义参数
val params = DownloadParam(downloadUrl, saveName, savePath)
val config = DownloadConfig()
config.baseUrl = "http://www.example.com"
val downloadTask = scope.download(params, config)
```

- 下载任务默认使用下载链接作为唯一标识，可以自定义

```kotlin
class CustomDownloadParam(url: String, saveName: String, savePath: String) :
    DownloadParam(url, saveName, savePath) {
    override fun tag(): String {
        //定义唯一标示
        return savePath + saveName
    }
}

//使用
val customParams = CustomDownloadParam(downloadUrl, saveName, savePath)
val downloadTask = scope.download(customParams)
```

- 在多个页面使用同样的标识创建任务时，将会返回同一个任务，因此可以在不同界面监听同一任务的进度和状态

## 状态和进度

通过```launchIn(scope)```指定监听所在的协程，以便于销毁监听

- 状态监听

```kotlin
//状态监听
downloadTask.state().onEach {
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
```

- 进度监听

```kotlin
//进度监听
downloadTask.progress().onEach {
    logD("name : $saveName , progress : ${it.percentStr()}")
}.launchIn(scope)
```

可以自定义监听间隔时间```downloadTask.state(500)```，```downloadTask.progress(500)```，默认200ms

## 任务操作

- 开始

```kotlin
downloadTask.start()
```

- 停止

```kotlin
downloadTask.stop()
```

- 删除（true : 删除文件，false : 不删除，默认false）

```kotlin
downloadTask.remove()
```
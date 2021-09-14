package com.zjh.download.helper

/**
 *  desc : 当前下载状态标识
 *  @author zjh
 *  on 2021/8/24
 */
sealed class State {
    var progress: Progress = Progress()
        internal set

    class None : State()
    class Waiting : State()
    class Downloading : State()
    class Stopped : State()
    class Failed : State()
    class Succeed : State()
}
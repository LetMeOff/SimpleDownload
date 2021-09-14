package com.zjh.download.helper

/**
 *  desc : 当前状态
 *  @author zjh
 *  on 2021/8/24
 */
class StateHolder {
    val none by lazy { State.None() }
    val waiting by lazy { State.Waiting() }
    val downloading by lazy { State.Downloading() }
    val stopped by lazy { State.Stopped() }
    val failed by lazy { State.Failed() }
    val succeed by lazy { State.Succeed() }

    var currentState: State = none

    fun isStarted(): Boolean {
        return currentState is State.Waiting || currentState is State.Downloading
    }

    fun isFailed(): Boolean {
        return currentState is State.Failed
    }

    fun isSucceed(): Boolean {
        return currentState is State.Succeed
    }

    fun canStart(): Boolean {
        return currentState is State.None || currentState is State.Failed || currentState is State.Stopped
    }

    fun isEnd(): Boolean {
        return currentState is State.None || currentState is State.Waiting || currentState is State.Stopped || currentState is State.Failed || currentState is State.Succeed
    }

    /**
     * 更新状态
     */
    fun updateState(new: State, progress: Progress): State {
        currentState = new.apply { this.progress = progress }
        return currentState
    }
}
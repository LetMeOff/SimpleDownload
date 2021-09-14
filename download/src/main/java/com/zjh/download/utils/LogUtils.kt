package com.zjh.download.utils

import android.util.Log

/**
 *  desc : log
 *  @author zjh
 *  on 2021/8/24
 */
const val TAG = "SimpleDownload"

private const val LOG_TYPE_i = "log_type_i"
private const val LOG_TYPE_D = "log_type_d"
private const val LOG_TYPE_V = "log_type_v"
private const val LOG_TYPE_W = "log_type_w"
private const val LOG_TYPE_E = "log_type_e"

fun logI(tag: String = TAG, message: Any) {
    printLog(tag, LOG_TYPE_i, message.toString())
}

fun logI(message: Any) {
    printLog(TAG, LOG_TYPE_i, message.toString())
}

fun logD(tag: String = TAG, message: Any) {
    printLog(tag, LOG_TYPE_D, message.toString())
}

fun logD(message: Any) {
    printLog(TAG, LOG_TYPE_D, message.toString())
}

fun logV(tag: String = TAG, message: Any) {
    printLog(tag, LOG_TYPE_V, message.toString())
}

fun logV(message: Any) {
    printLog(TAG, LOG_TYPE_V, message.toString())
}

fun logW(tag: String = TAG, message: Any) {
    printLog(tag, LOG_TYPE_W, message.toString())
}

fun logW(message: Any) {
    printLog(TAG, LOG_TYPE_W, message.toString())
}

fun logE(tag: String = TAG, message: Any) {
    printLog(tag, LOG_TYPE_E, message.toString())
}

fun logE(message: Any) {
    printLog(TAG, LOG_TYPE_E, message.toString())
}

fun <T> T.log(tag: String = TAG, logType: String = LOG_TYPE_E): T {
    if (this is Throwable) {
        printLog(tag, logType, message)
    } else {
        printLog(tag, logType, toString())
    }
    return this
}

private fun printLog(tag: String = TAG, logType: String, message: String?) {
    //栈堆追踪
    val stackTrace = Thread.currentThread().stackTrace
    //这个值不一定是4 当调用时的封装层数越多，此值也会越高
    val index = 4
    //类名
    val className = stackTrace[index].fileName
    //行数
    val lineNumber = stackTrace[index].lineNumber
    //方法名
    val methodName = stackTrace[index].methodName
    //append显示数据
    val stringBuilder = StringBuilder()
    stringBuilder.append("[ (").append(className).append(":").append(lineNumber).append(")#")
        .append(methodName).append(" ] ")
    //添加log
    stringBuilder.append(message)

    //打印日志
    when (logType) {
        LOG_TYPE_i -> Log.i(tag, stringBuilder.toString())
        LOG_TYPE_D -> Log.d(tag, stringBuilder.toString())
        LOG_TYPE_V -> Log.v(tag, stringBuilder.toString())
        LOG_TYPE_W -> Log.w(tag, stringBuilder.toString())
        LOG_TYPE_E -> Log.e(tag, stringBuilder.toString())
    }
}
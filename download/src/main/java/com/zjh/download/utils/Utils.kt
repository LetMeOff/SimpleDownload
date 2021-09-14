package com.zjh.download.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import java.io.File
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicInteger

/**
 *  desc : 单位转换
 *  @author zjh
 *  on 2021/8/24
 */

/**
 * 格式化文件大小
 */
fun Long.formatSize(): String {
    require(this >= 0) { "Size must larger than 0." }

    val byte = this.toDouble()
    val kb = byte / 1024.0
    val mb = byte / 1024.0 / 1024.0
    val gb = byte / 1024.0 / 1024.0 / 1024.0
    val tb = byte / 1024.0 / 1024.0 / 1024.0 / 1024.0

    return when {
        tb >= 1 -> "${tb.decimal(2)} TB"
        gb >= 1 -> "${gb.decimal(2)} GB"
        mb >= 1 -> "${mb.decimal(2)} MB"
        kb >= 1 -> "${kb.decimal(2)} KB"
        else -> "${byte.decimal(2)} B"
    }
}

/**
 * 保留两位小数点
 */
fun Double.decimal(digits: Int): Double {
    return this.toBigDecimal()
        .setScale(digits, BigDecimal.ROUND_HALF_UP)
        .toDouble()
}

/**
 * 百分比
 */
infix fun Long.ratio(bottom: Long): Double {
    if (bottom <= 0) {
        return 0.0
    }
    val result = (this * 100.0).toBigDecimal()
        .divide((bottom * 1.0).toBigDecimal(), 2, BigDecimal.ROUND_FLOOR)
    return result.toDouble()
}

fun String.toLongOrDefault(defaultValue: Long): Long {
    return try {
        toLong()
    } catch (_: NumberFormatException) {
        defaultValue
    }
}

suspend fun <T, R> (Collection<T>).parallel(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    max: Int = 2,
    action: suspend CoroutineScope.(T) -> R
): Iterable<R> = coroutineScope {
    val list = this@parallel
    if (list.isEmpty()) return@coroutineScope listOf<R>()

    val channel = Channel<T>()
    val output = Channel<R>()

    val counter = AtomicInteger(0)

    launch {
        list.forEach { channel.send(it) }
        channel.close()
    }

    repeat(max) {
        launch(dispatcher) {
            channel.consumeEach {
                output.send(action(it))
                val completed = counter.incrementAndGet()
                if (completed == list.size) {
                    output.close()
                }
            }
        }
    }

    val results = mutableListOf<R>()
    for (item in output) {
        results.add(item)
    }

    return@coroutineScope results
}

fun Context.installApk(file: File) {
    val intent = Intent(Intent.ACTION_VIEW)
    val authority = "$packageName.provider"
    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(this, authority, file)
    } else {
        Uri.fromFile(file)
    }
    intent.setDataAndType(uri, "application/vnd.android.package-archive")
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(intent)
}
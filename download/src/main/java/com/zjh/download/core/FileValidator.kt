package com.zjh.download.core

import com.zjh.download.utils.contentLength
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File

/**
 *  desc : 文件校验
 *  @author zjh
 *  on 2021/8/24
 */
interface FileValidator {
    fun validate(
        file: File,
        param: DownloadParam,
        response: Response<ResponseBody>
    ): Boolean
}

object DefaultFileValidator : FileValidator {
    override fun validate(
        file: File,
        param: DownloadParam,
        response: Response<ResponseBody>
    ): Boolean {
        return file.length() == response.contentLength()
    }
}
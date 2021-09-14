package com.zjh.download.helper

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface Api {
    @GET
    @Streaming
    suspend fun get(
        @Url url: String,
        @HeaderMap headers: Map<String, String>
    ): Response<ResponseBody>
}

interface HttpClientFactory {
    fun create(): OkHttpClient
}

object DefaultHttpClientFactory : HttpClientFactory {
    override fun create(): OkHttpClient {
        return OkHttpClient().newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()
    }
}

internal fun apiCreator(client: OkHttpClient, baseUrl: String): Api {
    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    return retrofit.create(Api::class.java)
}
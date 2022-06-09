package com.firemaples.youtubeplayertest.ui.webviewiframeapi

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

object ApiHub {
    private val retrofit by lazy {
        Retrofit.Builder().baseUrl("http://stackoverflow.com").build()
    }

    fun getAPI(): YoutubeAPI =
        retrofit.create(YoutubeAPI::class.java)
}

interface YoutubeAPI {
    @GET
    @Streaming
    fun getRaw(
        @Url
        url: String,
    ): Call<ResponseBody>
}

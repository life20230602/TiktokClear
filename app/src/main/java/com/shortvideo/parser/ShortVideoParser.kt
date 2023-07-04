package com.shortvideo.parser

import okhttp3.OkHttpClient
import okhttp3.Request

abstract class ShortVideoParser {

    abstract suspend fun getDownloadUrl(url: String): String

    companion object {
        val sClient: OkHttpClient = OkHttpClient.Builder().build()
        private const val tiktokUA =
            "com.ss.android.ugc.trill/494+Mozilla/5.0+(Linux;+Android+12;+2112123G+Build/SKQ1.211006.001;+wv)+AppleWebKit/537.36+(KHTML,+like+Gecko)+Version/4.0+Chrome/107.0.5304.105+Mobile+Safari/537.36"
        private const val douYinUA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36"
        private const val douYinAccept = "gzip, deflate, br"
        private const val douYinReferer = "https://www.douyin.com/"

        fun createRequest(url: String): Request.Builder? {
            if (url.contains("douyin")) {
                val builder = Request.Builder()
                builder.removeHeader("User-Agent")
                return builder
                    .url(url)
                    .addHeader("User-Agent", douYinUA)
                    .addHeader("Accept", douYinAccept)
                    .addHeader("Referer", douYinReferer)
            } else if (url.contains("tiktok")) {
                val builder = Request.Builder()
                builder.removeHeader("User-Agent")
                return builder.url(url).addHeader("User-Agent", tiktokUA)
            }
            return null
        }
    }

}
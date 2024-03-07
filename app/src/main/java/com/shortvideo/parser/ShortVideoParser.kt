package com.shortvideo.parser

import android.webkit.URLUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern

fun String.extractUrls(): List<String> {
    val urls: MutableList<String> = ArrayList()
    val pattern = Pattern.compile(
        "\\b(https?|ftp|file)://[-A-Z0-9+&@#/%?=~_|!:,.;]*[-A-Z0-9+&@#/%=~_|]",
        Pattern.CASE_INSENSITIVE
    )
    val matcher = pattern.matcher(this)
    while (matcher.find()) {
        urls.add(this.substring(matcher.start(0), matcher.end(0)))
    }
    return urls
}

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
            var newUrl = url
            if(!URLUtil.isHttpUrl(url) && !URLUtil.isHttpsUrl(url)){
                newUrl = url.extractUrls()[0]
            }
            if (newUrl.contains("douyin")) {
                val builder = Request.Builder()
                builder.removeHeader("User-Agent")
                return builder
                    .url(newUrl)
                    .addHeader("User-Agent", douYinUA)
                    .addHeader("Accept", douYinAccept)
                    .addHeader("Referer", douYinReferer)
            } else if (newUrl.contains("tiktok")) {
                val builder = Request.Builder()
                builder.removeHeader("User-Agent")
                return builder.url(newUrl).addHeader("User-Agent", tiktokUA)
            }
            return null
        }
    }

}
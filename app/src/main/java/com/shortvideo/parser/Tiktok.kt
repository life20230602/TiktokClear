package com.shortvideo.parser

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject


class Tiktok : ShortVideoParser() {
    override suspend fun getDownloadUrl(url: String): String = withContext(Dispatchers.IO) {
        var videoId = getVideoId(url)
        createRequest("https://api16-normal-c-useast1a.tiktokv.com/aweme/v1/feed/?aweme_id=$videoId")?.let {
            val request = it.build()
            val response = sClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    val jsonObj = JSONObject(body.string())
                    val videoData: JSONObject = jsonObj.optJSONArray("aweme_list").getJSONObject(0)
                    return@withContext videoData.optJSONObject("video")
                        .optJSONObject("play_addr").optJSONArray("url_list").optString(0)
                }
            }
        }
        return@withContext ""
    }

    /**
     * 转换链接
     */
    private fun convertUrl(url: String): String {
        createRequest(url)?.let {
            it.removeHeader("User-Agent")
            val response = sClient.newCall(
                it.addHeader(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 8.0.0; SM-G955U Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Mobile Safari/537.36"
                ).build()
            ).execute()
            if (response.isSuccessful) {
                return response.request().url().url().toString()
            }
        }
        return ""
    }

    private fun getVideoId(url: String): String? {
        var convertUrl = convertUrl(url)
        return Uri.parse(convertUrl).lastPathSegment
    }
}
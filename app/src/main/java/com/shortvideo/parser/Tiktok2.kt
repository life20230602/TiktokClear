package com.shortvideo.parser

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject


class Tiktok2 : ShortVideoParser() {
    override suspend fun getDownloadUrl(url: String): String = withContext(Dispatchers.IO) {
        createRequest("https://www.watermarkremover.io/api/video")?.let {
            val jsonObject = JSONObject()
            jsonObject.put("videoUrl",url)
            val requestBody =
                RequestBody.create(MediaType.parse("application/json"), jsonObject.toString())
            val request = it.post(requestBody).build()

            val response = sClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    return@withContext JSONObject(body.string()).optString("nowm")
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
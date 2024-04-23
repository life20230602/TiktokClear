package com.shortvideo.parser

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import org.json.JSONObject


class Tiktok2 : ShortVideoParser() {
    override suspend fun getDownloadUrl(url: String): String = withContext(Dispatchers.IO) {
        createRequest("https://api.twitterpicker.com/tiktok/mediav2?id=${Uri.parse(url).lastPathSegment}")?.let {
            try {
                val request = it.header("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer","https://tiktokdownloader.com/")
                    .get().build()
                val response = sClient.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        val resultJsonObject = JSONObject(body.string())
                        val mediasJsonObject = resultJsonObject.getJSONObject("video_no_watermark")
                        val url = mediasJsonObject.getString("url")
                        return@withContext url
                    }
                }
            }catch (e : Exception){
                return@withContext "解析失败，请重试"
            }
        }
        return@withContext ""
    }
}
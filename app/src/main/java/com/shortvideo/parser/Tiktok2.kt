package com.shortvideo.parser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import org.json.JSONObject


class Tiktok2 : ShortVideoParser() {
    override suspend fun getDownloadUrl(url: String): String = withContext(Dispatchers.IO) {
        createRequest("https://myapi.app/api/analyze")?.let {
            try {
                val builder = FormBody.Builder()
                builder.add("url",url)
                builder.add("sitename","tikmate.cc")
                val request = it.header("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36").header("Referer","https://tikmate.cc/")
                    .post(builder.build()).build()
                val response = sClient.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        val resultJsonObject = JSONObject(body.string())
                        val mediasJsonObject = resultJsonObject.getJSONArray("medias")
                        val length = mediasJsonObject.length()
                        var maxQualityUrl = ""
                        var maxQuality = 0
                        for (i in 0 until length) {
                            val itemObj = mediasJsonObject.getJSONObject(i)
                            val quality = itemObj.optString("quality", "0").toInt()
                            if(quality > maxQuality){
                                maxQuality = quality
                                maxQualityUrl = itemObj.optString("url")
                            }
                        }
                        return@withContext "https://myapi.app/api/download?sitename=tikmate.cc&url=$maxQualityUrl"
                    }
                }
            }catch (e : Exception){
                return@withContext "解析失败，请重试"
            }
        }
        return@withContext ""
    }
}
package com.shortvideo.parser

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.regex.Pattern


class Tiktok2 : ShortVideoParser() {
    override suspend fun getDownloadUrl(url: String): String = withContext(Dispatchers.IO) {
        createRequest("https://tiksave.io/api/ajaxSearch")?.let {
            try {
                val requestBody = FormBody.Builder().add("q",url);
                val request = it.header("User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer","https://tiksave.io")
                    .post(requestBody.build()).build()
                val response = sClient.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        val resultJsonObject = JSONObject(body.string()).getString("data")
                        val pattern = Pattern.compile("[a-zA-z]+://[^\\s]*")
                        val matcher = pattern.matcher(resultJsonObject)
                        var last = "";
                        while (matcher.find()){
                            last = matcher.group();
                        }
                        Log.e("====", "getDownloadUrl: "+last )
                        return@withContext last
                    }
                }
            }catch (e : Exception){
                return@withContext "解析失败，请重试"
            }
        }
        return@withContext ""
    }
}
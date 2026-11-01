package com.seoul.fmonair

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException

object SongAndLyricsService {
    private val client = OkHttpClient()

    suspend fun searchLyrics(trackName: String, artistName: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://lrclib.net/api/search?track_name=$trackName&artist_name=$artistName"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: return@withContext "가사를 찾을 수 없습니다."
                
                val jsonArray = JSONArray(body)
                if (jsonArray.length() > 0) {
                    val firstResult = jsonArray.getJSONObject(0)
                    firstResult.optString("syncedLyrics", firstResult.optString("plainLyrics", "가사 정보가 없습니다."))
                } else {
                    "가사 검색 결과가 없습니다."
                }
            } catch (e: Exception) {
                "가사를 불러오는 중 오류가 발생했습니다."
            }
        }
    }
}

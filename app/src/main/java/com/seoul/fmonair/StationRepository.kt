package com.seoul.fmonair

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

object StationRepository {
    val stations = listOf(
        RadioStation("kbs1", "KBS 1라디오", "97.3 MHz", "https://cfpwwwapi.kbs.co.kr/api/v1/landing/live/channel_code/21", true, "KBS"),
        RadioStation("kbs2", "KBS 해피FM", "106.1 MHz", "https://cfpwwwapi.kbs.co.kr/api/v1/landing/live/channel_code/22", true, "KBS"),
        RadioStation("kbs3", "KBS 쿨FM", "89.1 MHz", "https://cfpwwwapi.kbs.co.kr/api/v1/landing/live/channel_code/25", true, "KBS"),
        RadioStation("kbs4", "KBS 클래식FM", "93.1 MHz", "https://cfpwwwapi.kbs.co.kr/api/v1/landing/live/channel_code/24", true, "KBS"),
        RadioStation("mbc_sfm", "MBC 표준FM", "95.9 MHz", "https://sminiplay.imbc.com/aacplay.ashx?agent=webapp&channel=sfm", true, "MBC"),
        RadioStation("mbc_mfm", "MBC FM4U", "91.9 MHz", "https://sminiplay.imbc.com/aacplay.ashx?agent=webapp&channel=mfm", true, "MBC"),
        RadioStation("sbs_power", "SBS 파워FM", "107.7 MHz", "https://apis.sbs.co.kr/play-api/1.0/livestream/powerpc/powerfm?protocol=hls&ssl=Y", true, "SBS"),
        RadioStation("sbs_love", "SBS 러브FM", "103.5 MHz", "https://apis.sbs.co.kr/play-api/1.0/livestream/lovepc/lovefm?protocol=hls&ssl=Y", true, "SBS"),
        RadioStation("ebs", "EBS FM", "104.5 MHz", "https://ebsonair.ebs.co.kr/fmradiofamilypc/familypc1m/playlist.m3u8"),
        RadioStation("tbs", "TBS FM", "95.1 MHz", "https://cdnfm.tbs.seoul.kr/tbs/_definst_/tbs_fm_web_360.smil/playlist.m3u8"),
        RadioStation("tbs_e", "TBS eFM", "101.3 MHz", "https://cdnefm.tbs.seoul.kr/tbs/_definst_/tbs_efm_web_360.smil/playlist.m3u8"),
        RadioStation("ytn", "YTN 라디오", "94.5 MHz", "https://radiolive.ytn.co.kr/radio/_definst_/20211118_fmlive/playlist.m3u8"),
        RadioStation("gugak", "국악방송 FM", "99.1 MHz", "https://mgugaklive.nowcdn.co.kr/gugakradio/gugakradio.stream/playlist.m3u8"),
        RadioStation("cbs_sfm", "CBS 표준FM", "98.1 MHz", "https://maac.cbs.co.kr/cbs981/_definst_/cbs981.stream/playlist.m3u8"),
        RadioStation("cbs_mfm", "CBS 음악FM", "93.9 MHz", "https://maac.cbs.co.kr/cbs939/_definst_/cbs939.stream/playlist.m3u8"),
        RadioStation("febc", "FEBC 극동방송", "106.9 MHz", "https://mlive1.febc.net/live/seoulfm/playlist.m3u8"),
        RadioStation("bbs", "BBS 불교방송", "101.9 MHz", "https://bbslive.clouducs.com/bbsradio-live/livestream/playlist.m3u8"),
        RadioStation("cpbc", "CPBC 평화방송", "105.3 MHz", "https://onair.cpbc.co.kr/live/fm/playlist.m3u8")
    )

    private val client = OkHttpClient()

    suspend fun resolvePlayableStreamUrl(station: RadioStation): String {
        if (!station.isDynamic) return station.streamUrl

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(station.streamUrl).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: return@withContext ""

                when (station.dynamicType) {
                    "KBS" -> {
                        val json = JSONObject(body)
                        val channelItem = json.getJSONArray("channel_item").getJSONObject(0)
                        channelItem.getString("service_url")
                    }
                    "MBC", "SBS" -> {
                        // Returns the raw string containing the .m3u8 URL with token
                        body.trim()
                    }
                    else -> ""
                }
            } catch (e: IOException) {
                e.printStackTrace()
                ""
            }
        }
    }
}

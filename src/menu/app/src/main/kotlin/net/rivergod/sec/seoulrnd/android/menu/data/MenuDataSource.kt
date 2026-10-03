package net.rivergod.sec.seoulrnd.android.menu.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/** 하루치 식단을 getSeoulRndMenuList.do 응답과 같은 형식의 JSON 문자열로 돌려준다. */
interface MenuDataSource {
    /** 실제 서버가 아닌 추정(예시) 데이터인지 여부. 화면에 표시하는 용도. */
    val isSample: Boolean

    suspend fun fetch(date: LocalDate): String
}

/**
 * 0.9.14 (2024-11-01) 에서 사용하던 웰스토리 API.
 * 현재는 동작하지 않는 것으로 확인되어 기본값으로 사용하지 않는다. (빌드 설정 seoulrnd.menuSource=remote)
 */
class WelstoryRemoteDataSource(
    private val client: OkHttpClient = OkHttpClient.Builder()
        // 0.9.14 는 5초 동안 응답이 없으면 "Server 응답이 없습니다." 를 띄웠다.
        .callTimeout(5, TimeUnit.SECONDS)
        .build(),
) : MenuDataSource {

    override val isSample = false

    override suspend fun fetch(date: LocalDate): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(URL)
            .post(
                FormBody.Builder()
                    .add("meal_type", "2")
                    .add("course", "AA")
                    .add("dt", date.format(DateTimeFormatter.BASIC_ISO_DATE))
                    .add("dtFlag", "1")
                    .add("hallNm", "seoulrnd")
                    .add("engYn", "N")
                    .build()
            )
            .addHeader("User-Agent", USER_AGENT)
            .addHeader("Referer", REFERER)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }
            response.body?.string() ?: throw IOException("Empty body")
        }
    }

    private companion object {
        const val URL = "https://www.samsungwelstory.com/menu/getSeoulRndMenuList.do?="
        const val REFERER = "https://www.samsungwelstory.com/menu/seoulrnd/menu.jsp"
        const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
    }
}

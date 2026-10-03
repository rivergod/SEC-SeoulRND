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
 * 웰스토리 우면 R&D 메뉴 API. 메뉴 안내 페이지(MENU_PAGE_URL)가 같은 요청으로 메뉴를 불러온다.
 * 0.9.14 의 www.samsungwelstory.com 주소는 더 이상 동작하지 않고 welmenu.welstory.com 으로 옮겨졌다. (2026-10 확인)
 */
class WelstoryRemoteDataSource(
    private val client: OkHttpClient = OkHttpClient.Builder()
        // 0.9.14 는 5초 동안 응답이 없으면 "Server 응답이 없습니다." 를 띄웠다.
        // 지금은 하루치 응답이 T/O 상품 목록까지 포함해 ~370KB 라 느린 망을 고려해 10초로 둔다.
        .callTimeout(10, TimeUnit.SECONDS)
        .build(),
) : MenuDataSource {

    override val isSample = false

    override suspend fun fetch(date: LocalDate): String = withContext(Dispatchers.IO) {
        val dt = date.format(DateTimeFormatter.BASIC_ISO_DATE)
        // 메뉴 안내 페이지의 meal_search 폼과 같은 값. meal_type 과 무관하게 하루치 세 끼가 모두 온다.
        val request = Request.Builder()
            .url(API_URL)
            .post(
                FormBody.Builder()
                    .add("meal_type", "2")
                    .add("course", "AA")
                    .add("dt", dt)
                    .add("menuDt", dt)
                    .add("dtFlag", "")
                    .add("hallNm", "seoulrnd")
                    .add("engYn", "N")
                    .build()
            )
            .addHeader("User-Agent", USER_AGENT)
            .addHeader("Referer", MENU_PAGE_URL)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }
            response.body?.string() ?: throw IOException("Empty body")
        }
    }

    private companion object {
        const val API_URL = "https://welmenu.welstory.com/menu/getSeoulRndMenuList.do"
        const val MENU_PAGE_URL = "https://welmenu.welstory.com/menu/seoulrnd/menu.jsp"
        // 웹 방화벽(Imperva)을 거치므로 0.9.14 와 같이 브라우저 User-Agent 를 보낸다.
        const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
    }
}

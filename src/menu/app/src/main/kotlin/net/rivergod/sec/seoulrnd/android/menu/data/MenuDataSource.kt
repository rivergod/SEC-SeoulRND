package net.rivergod.sec.seoulrnd.android.menu.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.rivergod.sec.seoulrnd.android.menu.dto.Cafeteria
import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/** 한 식당의 하루치 메뉴를 불러온다. */
interface MenuDataSource {
    val cafeteria: Cafeteria

    /** 실제 서버가 아닌 예시 데이터인지 여부. 화면에 표시하는 용도. */
    val isSample: Boolean

    suspend fun fetch(date: LocalDate): List<CuisineDTO>
}

/** 두 식당의 메뉴 API 가 같이 쓰는 HTTP 호출. 두 API 모두 메뉴판 웹페이지가 쓰는 form POST 다. */
internal object MenuHttp {

    val client: OkHttpClient = OkHttpClient.Builder()
        // 0.9.14 는 5초 동안 응답이 없으면 "Server 응답이 없습니다." 를 띄웠다.
        // 지금은 웰스토리 하루치 응답이 T/O 상품 목록까지 포함해 ~370KB 라 느린 망을 고려해 10초로 둔다.
        .callTimeout(10, TimeUnit.SECONDS)
        .build()

    // 웰스토리는 웹 방화벽(Imperva)을 거치므로 0.9.14 와 같이 브라우저 User-Agent 를 보낸다.
    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"

    suspend fun postForm(url: String, referer: String, form: Map<String, String>): String = withContext(Dispatchers.IO) {
        val body = FormBody.Builder().apply { form.forEach { (k, v) -> add(k, v) } }.build()
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("User-Agent", USER_AGENT)
            .addHeader("Referer", referer)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }
            response.body?.string() ?: throw IOException("Empty body")
        }
    }

    fun yyyyMMdd(date: LocalDate): String = date.format(DateTimeFormatter.BASIC_ISO_DATE)
}

/**
 * 2식당 — 웰스토리 우면 R&D 메뉴 API. 메뉴 안내 페이지(MENU_PAGE_URL)가 같은 요청으로 메뉴를 불러온다.
 * 0.9.14 의 www.samsungwelstory.com 주소는 더 이상 동작하지 않고 welmenu.welstory.com 으로 옮겨졌다. (2026-10 확인)
 */
class WelstoryRemoteDataSource : MenuDataSource {

    override val cafeteria = Cafeteria.CAFETERIA_2
    override val isSample = false

    override suspend fun fetch(date: LocalDate): List<CuisineDTO> {
        val dt = MenuHttp.yyyyMMdd(date)
        // 메뉴 안내 페이지의 meal_search 폼과 같은 값. meal_type 과 무관하게 하루치 세 끼가 모두 온다.
        val json = MenuHttp.postForm(
            API_URL, MENU_PAGE_URL,
            mapOf(
                "meal_type" to "2",
                "course" to "AA",
                "dt" to dt,
                "menuDt" to dt,
                "dtFlag" to "",
                "hallNm" to "seoulrnd",
                "engYn" to "N",
            )
        )
        return withContext(Dispatchers.Default) { WelstoryMenuParser.parse(json) }
    }

    private companion object {
        const val API_URL = "https://welmenu.welstory.com/menu/getSeoulRndMenuList.do"
        const val MENU_PAGE_URL = "https://welmenu.welstory.com/menu/seoulrnd/menu.jsp"
    }
}

/** 1식당 — 풀무원 오늘 식단표 API. 메뉴판 페이지(MENU_PAGE_URL)의 todayMealPlan.js 와 같은 요청을 보낸다. */
class PulmuoneRemoteDataSource : MenuDataSource {

    override val cafeteria = Cafeteria.CAFETERIA_1
    override val isSample = false

    override suspend fun fetch(date: LocalDate): List<CuisineDTO> {
        val json = MenuHttp.postForm(
            API_URL, MENU_PAGE_URL,
            mapOf(
                // 운영사·사업장(삼성전자 서울R&D캠퍼스 1식당) 코드. todayMealPlan.js 에 고정되어 있는 값
                "operCd" to "O000002",
                "assignCd" to "S000716",
                "langGb" to "KR",
                "serachDay" to MenuHttp.yyyyMMdd(date),
            )
        )
        return withContext(Dispatchers.Default) { PulmuoneMenuParser.parse(json) }
    }

    private companion object {
        const val API_URL = "https://puls2.pulmuone.com/src/sql/menu/todayMealPlan_sql.php"
        const val MENU_PAGE_URL = "https://puls2.pulmuone.com/src/php/menu/todayMealPlan.php"
    }
}

package net.rivergod.sec.seoulrnd.android.menu.data

import net.rivergod.sec.seoulrnd.android.menu.dto.Cafeteria
import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * 예시 식단 데이터 (빌드 설정 seoulrnd.menuSource=sample). 네트워크 없이 화면을 개발·확인할 때 쓴다.
 *
 * 식당마다 실제 API 와 같은 응답 형식(필드 일부만 채움)으로 만들어 실제 파서를 거친다.
 * 메뉴는 날짜에 따라 돌아가며 바뀌고, 실제 서버와 달리 주말에도 메뉴를 돌려준다.
 */
class SampleMenuDataSource(override val cafeteria: Cafeteria) : MenuDataSource {

    override val isSample = true

    override suspend fun fetch(date: LocalDate): List<CuisineDTO> = when (cafeteria) {
        Cafeteria.CAFETERIA_1 -> PulmuoneMenuParser.parse(pulmuoneJson(date))
        Cafeteria.CAFETERIA_2 -> WelstoryMenuParser.parse(welstoryJson(date))
    }

    /** todayMealPlan_sql.php 형식: 17칸 배열 행, 주메뉴(Y) 다음에 주메뉴와 같은 이름을 포함한 구성 메뉴(N) */
    fun pulmuoneJson(date: LocalDate): String {
        val rows = JSONArray()
        forEachDish(PULMUONE_COURSES, date) { course, dish ->
            fun row(name: String, totKcal: String, mainYn: String) = JSONArray(
                listOf(
                    MenuHttp.yyyyMMdd(date), null, null, null, "", course.mealType, "", "", course.courseType,
                    course.courseTxt, "", name, "", "", null, totKcal, mainYn,
                )
            )
            rows.put(row(dish.main, dish.kcal, "Y"))
            rows.put(row(dish.main, "0", "N"))
            dish.sides.forEach { rows.put(row(it, "0", "N")) }
        }
        return JSONObject().put("mealData", JSONObject().put("mealData", rows)).toString()
    }

    /** getSeoulRndMenuList.do 형식: 대표 메뉴(typical_menu=Y) 다음에 곁들임 메뉴 */
    fun welstoryJson(date: LocalDate): String {
        val rows = JSONArray()
        forEachDish(WELSTORY_COURSES, date) { course, dish ->
            fun row(typical: Boolean, name: String, kcal: String) = JSONObject()
                .put("menu_meal_type", course.mealType)
                .put("menu_course_type", course.courseType)
                .put("hall_no", course.hallNo)
                .put("course_txt", course.courseTxt)
                .put("typical_menu", if (typical) "Y" else "N")
                .put("menu_name", name)
                .put("tot_kcal", kcal)
            rows.put(row(typical = true, name = dish.main, kcal = dish.kcal))
            dish.sides.forEach { rows.put(row(typical = false, name = it, kcal = "")) }
        }
        return rows.toString()
    }

    private fun forEachDish(courses: List<Course>, date: LocalDate, block: (Course, Dish) -> Unit) {
        val day = date.toEpochDay()
        courses.forEachIndexed { index, course ->
            block(course, course.dishes[Math.floorMod(day + index, course.dishes.size.toLong()).toInt()])
        }
    }

    private class Dish(val main: String, val kcal: String, val sides: List<String>)

    /** @param hallNo 웰스토리만 사용 */
    private class Course(
        val mealType: String,
        val courseType: String,
        val hallNo: String,
        val courseTxt: String,
        val dishes: List<Dish>,
    )

    companion object {
        fun all(): List<MenuDataSource> = Cafeteria.entries.map { SampleMenuDataSource(it) }

        private const val BREAKFAST = "1"
        private const val LUNCH = "2"
        private const val DINNER = "3"

        // 웰스토리 실제 응답의 hall_no: E5J2, E5J3 식당 코너 / E5J4 Take Out
        private const val HALL_DINE_IN = "E5J2"
        private val HALL_TAKE_OUT = WelstoryMenuParser.TAKE_OUT_HALL_NOS.first()

        private fun dish(main: String, kcal: Int, vararg sides: String) = Dish(main, kcal.toString(), sides.toList())

        /** 1식당(풀무원). 코너 구성은 2026-10-02 실제 메뉴판 기준. 끼니 코드 001 조식 / 002 중식 / 003 석식 */
        private val PULMUONE_COURSES = listOf(
            Course(
                "001", "029", "", "죽or수프＆찜채소", listOf(
                    dish("소고기미역죽", 394, "찜채소(쥬키니)"),
                    dish("양송이크림수프", 420, "찜채소(단호박)", "모닝빵"),
                )
            ),
            Course(
                "001", "014", "", "T/O 샌드위치", listOf(
                    dish("에그마요샌드위치", 520, "우유"),
                    dish("햄치즈샌드위치", 480, "두유"),
                )
            ),
            Course(
                "002", "001", "", "ASIAN", listOf(
                    dish("들깨칼국수", 1097, "미니열무비빔밥", "수제녹두빈대떡", "해초무침"),
                    dish("소고기쌀국수", 880, "짜조", "숙주무침"),
                )
            ),
            Course(
                "002", "003", "", "쉐피스트", listOf(
                    dish("제육돈까스", 1229, "쌀밥", "쑥갓어묵국", "산더미샐러드＆드레싱"),
                    dish("치즈함박스테이크", 1150, "버터라이스", "콘스프", "코울슬로"),
                )
            ),
            Course(
                "002", "004", "", "보글보글", listOf(
                    dish("모듬순대전골", 1286, "수제비사리"),
                    dish("부대찌개", 1180, "라면사리", "쌀밥"),
                )
            ),
            Course(
                "002", "005", "", "88℃온도", listOf(
                    dish("미나리닭곰탕", 1063, "잡곡밥", "떡새우완자전", "오이양파무침"),
                    dish("뼈해장국", 990, "쌀밥", "깍두기", "부추무침"),
                )
            ),
            Course(
                "002", "007", "", "풀스바", listOf(
                    dish("[페스코]구운연어영양밥", 567, "뿌리채소조림", "두부면파프리카볶음", "채소믹스샐러드"),
                    dish("[비건]두부스테이크덮밥", 540, "구운채소", "그린샐러드"),
                )
            ),
            Course(
                "002", "008", "", "찬장", listOf(
                    dish("뚝배기순두부찌개", 1351, "잡곡밥", "돼지고기모듬장조림", "멸치볶음", "숙주들깨나물"),
                    dish("된장찌개", 1120, "쌀밥", "고등어구이", "시금치나물"),
                )
            ),
            Course(
                "002", "018", "", "T/O 밀박스", listOf(
                    dish("불고기도시락", 820, "미니샐러드"),
                    dish("치킨마요덮밥", 860, "미소장국"),
                )
            ),
            Course(
                "003", "020", "", "T/O 치킨", listOf(
                    dish("후라이드치킨", 1100, "치킨무", "콜라"),
                    dish("양념치킨", 1180, "치킨무", "사이다"),
                )
            ),
            Course(
                "003", "036", "", "찜채소", listOf(
                    dish("찜채소(쥬키니)", 205, "초간장"),
                    dish("찜채소(양배추)", 180, "쌈장"),
                )
            ),
        )

        /** 2식당(웰스토리) */
        private val WELSTORY_COURSES = listOf(
            // ---- 조식 ----
            Course(
                BREAKFAST, "AA", HALL_DINE_IN, "봄이온소반", listOf(
                    dish("북어해장국", 650, "쌀밥", "계란말이", "시금치나물", "깍두기"),
                    dish("소고기무국", 620, "쌀밥", "두부조림", "김구이", "배추김치"),
                    dish("순두부찌개", 640, "잡곡밥", "어묵볶음", "콩나물무침", "깍두기"),
                )
            ),
            Course(
                BREAKFAST, "AB", HALL_DINE_IN, "헬스기빙 Korean", listOf(
                    dish("전복죽", 420, "백김치", "장조림", "요구르트"),
                    dish("단호박죽", 380, "동치미", "메추리알조림", "사과"),
                )
            ),
            Course(
                BREAKFAST, "CB", HALL_TAKE_OUT, "T/O 샌드위치", listOf(
                    dish("햄치즈 샌드위치", 480, "우유", "바나나"),
                    dish("에그마요 샌드위치", 520, "두유", "방울토마토"),
                    dish("닭가슴살 샐러드", 350, "오렌지주스", "삶은계란"),
                )
            ),

            // ---- 중식 ----
            Course(
                LUNCH, "AA", HALL_DINE_IN, "고슬고슬 비빈", listOf(
                    dish("돌솥비빔밥", 780, "유부장국", "계란후라이", "배추김치"),
                    dish("참치마요덮밥", 820, "미소장국", "단무지무침"),
                    dish("제육덮밥", 850, "계란국", "깍두기"),
                )
            ),
            Course(
                LUNCH, "AB", HALL_DINE_IN, "도담찌개", listOf(
                    dish("돼지김치찌개", 760, "쌀밥", "계란찜", "콩나물무침", "깍두기"),
                    dish("차돌된장찌개", 740, "쌀밥", "고등어구이", "오이무침"),
                    dish("부대찌개", 830, "쌀밥", "라면사리", "단무지"),
                )
            ),
            Course(
                LUNCH, "AC", HALL_DINE_IN, "우리미각면", listOf(
                    dish("냉모밀", 610, "유부초밥", "단무지"),
                    dish("잔치국수", 590, "김밥", "배추김치"),
                    dish("바지락칼국수", 650, "보리밥", "겉절이"),
                )
            ),
            Course(
                LUNCH, "AD", HALL_DINE_IN, "헬스기빙 Specialty", listOf(
                    dish("연어포케", 560, "현미밥", "그린샐러드", "요거트"),
                    dish("닭가슴살 스테이크", 540, "귀리밥", "구운채소", "두유"),
                )
            ),
            Course(
                LUNCH, "AE", HALL_DINE_IN, "봄이온소반", listOf(
                    dish("소불고기", 860, "쌀밥", "미역국", "잡채", "배추김치"),
                    dish("고등어조림", 780, "쌀밥", "된장국", "계란말이", "깍두기"),
                    dish("닭볶음탕", 820, "쌀밥", "어묵국", "감자채볶음"),
                )
            ),
            Course(
                LUNCH, "AF", HALL_DINE_IN, "가츠앤", listOf(
                    dish("등심돈까스", 950, "미니우동", "양배추샐러드", "단무지"),
                    dish("치즈돈까스", 1020, "미소장국", "마카로니샐러드"),
                    dish("생선까스", 880, "크림스프", "코울슬로"),
                )
            ),
            Course(
                LUNCH, "AG", HALL_DINE_IN, "싱푸차이나", listOf(
                    dish("짜장면", 880, "미니탕수육", "단무지", "양파"),
                    dish("짬뽕", 820, "군만두", "짜사이"),
                    dish("마파두부덮밥", 790, "계란탕", "짜사이"),
                )
            ),
            Course(
                LUNCH, "AH", HALL_DINE_IN, "브라운그릴", listOf(
                    dish("함박스테이크", 970, "버터라이스", "콘샐러드", "양송이스프"),
                    dish("치킨스테이크", 900, "감자튀김", "코울슬로"),
                )
            ),
            Course(
                LUNCH, "AI", HALL_DINE_IN, "아시안픽스", listOf(
                    dish("소고기 쌀국수", 640, "짜조", "숙주무침"),
                    dish("팟타이", 780, "춘권", "피클"),
                    dish("나시고렝", 820, "치킨사테", "오이피클"),
                )
            ),
            Course(
                LUNCH, "AJ", HALL_DINE_IN, "나폴리폴리", listOf(
                    dish("토마토 미트볼 파스타", 890, "마늘빵", "피클"),
                    dish("까르보나라", 950, "갈릭브레드", "콘샐러드"),
                    dish("마르게리타 피자", 870, "시저샐러드", "피클"),
                )
            ),
            Course(
                LUNCH, "AK", HALL_DINE_IN, "탕맛기픈", listOf(
                    dish("갈비탕", 780, "쌀밥", "깍두기", "부추무침"),
                    dish("순대국", 820, "쌀밥", "깍두기", "새우젓"),
                    dish("육개장", 760, "쌀밥", "김치전", "배추김치"),
                )
            ),
            Course(
                LUNCH, "AL", HALL_DINE_IN, "슬로우팟", listOf(
                    dish("소고기 카레라이스", 830, "후쿠진즈케", "미소장국"),
                    dish("하이라이스", 810, "양배추샐러드", "단무지"),
                )
            ),
            Course(
                LUNCH, "AM", HALL_DINE_IN, "스냅스낵", listOf(
                    dish("참치김밥", 520, "라면", "단무지"),
                    dish("떡볶이", 610, "순대", "모둠튀김"),
                )
            ),
            Course(
                LUNCH, "CB", HALL_TAKE_OUT, "T/O 샌드위치", listOf(
                    dish("불고기 샌드위치[2~3Coin]", 560, "두유"),
                    dish("연어 샐러드[2~3Coin]", 420, "착즙주스"),
                )
            ),

            // ---- 석식 ----
            Course(
                DINNER, "AA", HALL_DINE_IN, "봄이온소반", listOf(
                    dish("닭갈비", 820, "쌀밥", "콩나물국", "배추김치"),
                    dish("삼치구이", 720, "쌀밥", "된장국", "감자조림"),
                    dish("훈제오리", 860, "쌀밥", "무쌈", "쌈채소"),
                )
            ),
            Course(
                DINNER, "AB", HALL_DINE_IN, "도담찌개", listOf(
                    dish("참치김치찌개", 750, "쌀밥", "계란말이", "김구이"),
                    dish("순두부찌개", 700, "쌀밥", "어묵볶음", "깍두기"),
                )
            ),
            Course(
                DINNER, "AC", HALL_DINE_IN, "헬스기빙365", listOf(
                    dish("두부스테이크", 520, "현미밥", "그린샐러드", "단호박찜"),
                    dish("닭가슴살 샐러드볼", 480, "군고구마", "요거트"),
                )
            ),
            Course(
                DINNER, "GG", HALL_TAKE_OUT, "T/O 밀박스", listOf(
                    dish("제육볶음도시락[세트]", 840, "미역국", "쌈채소"),
                    dish("새우연어솥밥도시락[세트]", 774, "미소장국", "단무지"),
                )
            ),
        )
    }
}

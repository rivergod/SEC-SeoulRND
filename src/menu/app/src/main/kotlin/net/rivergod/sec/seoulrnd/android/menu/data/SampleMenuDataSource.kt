package net.rivergod.sec.seoulrnd.android.menu.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * 추정 식단 데이터.
 *
 * 웰스토리 API 가 더 이상 응답하지 않아 실제 데이터를 받을 수 없으므로,
 * 0.9.14 의 파싱 코드에서 역으로 추정한 응답 형식(getSeoulRndMenuList.do)으로 예시 데이터를 만든다.
 * 코너 구성은 식당 안내도(drawable/menu_position)와 아이콘 리소스를 기준으로 했고,
 * 메뉴는 날짜에 따라 돌아가며 바뀐다.
 */
class SampleMenuDataSource : MenuDataSource {

    override val isSample = true

    override suspend fun fetch(date: LocalDate): String = buildJson(date).toString()

    fun buildJson(date: LocalDate): JSONArray {
        val day = date.toEpochDay()
        val rows = JSONArray()
        COURSES.forEachIndexed { index, course ->
            val dish = course.dishes[Math.floorMod(day + index, course.dishes.size.toLong()).toInt()]
            rows.put(course.row(typical = true, name = dish.main, kcal = dish.kcal))
            dish.sides.forEach { rows.put(course.row(typical = false, name = it, kcal = "")) }
        }
        return rows
    }

    private class Dish(val main: String, val kcal: String, val sides: List<String>)

    private class Course(
        val mealType: String,
        val courseType: String,
        val hallNo: String,
        val courseTxt: String,
        val dishes: List<Dish>,
    ) {
        fun row(typical: Boolean, name: String, kcal: String) = JSONObject()
            .put("menu_meal_type", mealType)
            .put("menu_course_type", courseType)
            .put("hall_no", hallNo)
            .put("course_txt", courseTxt)
            .put("typical_menu", if (typical) "Y" else "N")
            .put("menu_name", name)
            .put("tot_kcal", kcal)
    }

    private companion object {
        const val BREAKFAST = "1"
        const val LUNCH = "2"
        const val DINNER = "3"

        const val HALL_CAMPUS_2 = "SRND2"
        val HALL_CAMPUS_1 = WelstoryMenuParser.CAMPUS_1_HALL_NOS.first()

        fun dish(main: String, kcal: Int, vararg sides: String) = Dish(main, kcal.toString(), sides.toList())

        val COURSES = listOf(
            // ---- 조식 ----
            Course(
                BREAKFAST, "AA", HALL_CAMPUS_2, "봄이온소반", listOf(
                    dish("북어해장국", 650, "쌀밥", "계란말이", "시금치나물", "깍두기"),
                    dish("소고기무국", 620, "쌀밥", "두부조림", "김구이", "배추김치"),
                    dish("순두부찌개", 640, "잡곡밥", "어묵볶음", "콩나물무침", "깍두기"),
                )
            ),
            Course(
                BREAKFAST, "AB", HALL_CAMPUS_2, "헬스기빙 Korean", listOf(
                    dish("전복죽", 420, "백김치", "장조림", "요구르트"),
                    dish("단호박죽", 380, "동치미", "메추리알조림", "사과"),
                )
            ),
            Course(
                BREAKFAST, "AC", HALL_CAMPUS_2, "Take me Out", listOf(
                    dish("햄치즈 샌드위치", 480, "우유", "바나나"),
                    dish("에그마요 샌드위치", 520, "두유", "방울토마토"),
                    dish("닭가슴살 샐러드", 350, "오렌지주스", "삶은계란"),
                )
            ),

            // ---- 중식 : 2캠퍼스 (D,E,F Tower) ----
            Course(
                LUNCH, "AA", HALL_CAMPUS_2, "고슬고슬 비빈", listOf(
                    dish("돌솥비빔밥", 780, "유부장국", "계란후라이", "배추김치"),
                    dish("참치마요덮밥", 820, "미소장국", "단무지무침"),
                    dish("제육덮밥", 850, "계란국", "깍두기"),
                )
            ),
            Course(
                LUNCH, "AB", HALL_CAMPUS_2, "도담찌개", listOf(
                    dish("돼지김치찌개", 760, "쌀밥", "계란찜", "콩나물무침", "깍두기"),
                    dish("차돌된장찌개", 740, "쌀밥", "고등어구이", "오이무침"),
                    dish("부대찌개", 830, "쌀밥", "라면사리", "단무지"),
                )
            ),
            Course(
                LUNCH, "AC", HALL_CAMPUS_2, "우리미각면", listOf(
                    dish("냉모밀", 610, "유부초밥", "단무지"),
                    dish("잔치국수", 590, "김밥", "배추김치"),
                    dish("바지락칼국수", 650, "보리밥", "겉절이"),
                )
            ),
            Course(
                LUNCH, "AD", HALL_CAMPUS_2, "헬스기빙 Specialty", listOf(
                    dish("연어포케", 560, "현미밥", "그린샐러드", "요거트"),
                    dish("닭가슴살 스테이크", 540, "귀리밥", "구운채소", "두유"),
                )
            ),
            Course(
                LUNCH, "AE", HALL_CAMPUS_2, "봄이온소반", listOf(
                    dish("소불고기", 860, "쌀밥", "미역국", "잡채", "배추김치"),
                    dish("고등어조림", 780, "쌀밥", "된장국", "계란말이", "깍두기"),
                    dish("닭볶음탕", 820, "쌀밥", "어묵국", "감자채볶음"),
                )
            ),
            Course(
                LUNCH, "AF", HALL_CAMPUS_2, "가츠엔", listOf(
                    dish("등심돈까스", 950, "미니우동", "양배추샐러드", "단무지"),
                    dish("치즈돈까스", 1020, "미소장국", "마카로니샐러드"),
                    dish("생선까스", 880, "크림스프", "코울슬로"),
                )
            ),
            Course(
                LUNCH, "AG", HALL_CAMPUS_2, "싱푸차이나", listOf(
                    dish("짜장면", 880, "미니탕수육", "단무지", "양파"),
                    dish("짬뽕", 820, "군만두", "짜사이"),
                    dish("마파두부덮밥", 790, "계란탕", "짜사이"),
                )
            ),
            Course(
                LUNCH, "AH", HALL_CAMPUS_2, "브라운그릴", listOf(
                    dish("함박스테이크", 970, "버터라이스", "콘샐러드", "양송이스프"),
                    dish("치킨스테이크", 900, "감자튀김", "코울슬로"),
                )
            ),
            Course(
                LUNCH, "AI", HALL_CAMPUS_2, "아시안픽스", listOf(
                    dish("소고기 쌀국수", 640, "짜조", "숙주무침"),
                    dish("팟타이", 780, "춘권", "피클"),
                    dish("나시고렝", 820, "치킨사테", "오이피클"),
                )
            ),
            Course(
                LUNCH, "AJ", HALL_CAMPUS_2, "나폴리폴리", listOf(
                    dish("토마토 미트볼 파스타", 890, "마늘빵", "피클"),
                    dish("까르보나라", 950, "갈릭브레드", "콘샐러드"),
                    dish("마르게리타 피자", 870, "시저샐러드", "피클"),
                )
            ),
            Course(
                LUNCH, "AK", HALL_CAMPUS_2, "탕맛기픈", listOf(
                    dish("갈비탕", 780, "쌀밥", "깍두기", "부추무침"),
                    dish("순대국", 820, "쌀밥", "깍두기", "새우젓"),
                    dish("육개장", 760, "쌀밥", "김치전", "배추김치"),
                )
            ),
            Course(
                LUNCH, "AL", HALL_CAMPUS_2, "슬로우팟", listOf(
                    dish("소고기 카레라이스", 830, "후쿠진즈케", "미소장국"),
                    dish("하이라이스", 810, "양배추샐러드", "단무지"),
                )
            ),
            Course(
                LUNCH, "AM", HALL_CAMPUS_2, "스냅스낵", listOf(
                    dish("참치김밥", 520, "라면", "단무지"),
                    dish("떡볶이", 610, "순대", "모둠튀김"),
                )
            ),
            Course(
                LUNCH, "AN", HALL_CAMPUS_2, "Take me Out", listOf(
                    dish("불고기 샌드위치", 560, "두유"),
                    dish("연어 샐러드", 420, "착즙주스"),
                )
            ),

            // ---- 중식 : 1캠퍼스 (A,B,C Tower) ----
            Course(
                LUNCH, "BA", HALL_CAMPUS_1, "봄이온소반", listOf(
                    dish("오징어볶음", 790, "쌀밥", "콩나물국", "계란말이", "배추김치"),
                    dish("돼지갈비찜", 880, "쌀밥", "미역국", "숙주나물"),
                )
            ),
            Course(
                LUNCH, "BB", HALL_CAMPUS_1, "도담찌개", listOf(
                    dish("청국장", 720, "쌀밥", "고등어구이", "배추김치"),
                    dish("동태찌개", 700, "쌀밥", "계란찜", "깍두기"),
                )
            ),
            Course(
                LUNCH, "BC", HALL_CAMPUS_1, "가츠엔", listOf(
                    dish("치킨까스", 920, "미니우동", "양배추샐러드"),
                    dish("카츠동", 900, "미소장국", "단무지"),
                )
            ),
            Course(
                LUNCH, "BD", HALL_CAMPUS_1, "싱푸차이나", listOf(
                    dish("새우볶음밥", 850, "짬뽕국물", "군만두"),
                    dish("유산슬밥", 800, "계란탕", "짜사이"),
                )
            ),

            // ---- 석식 ----
            Course(
                DINNER, "AA", HALL_CAMPUS_2, "봄이온소반", listOf(
                    dish("닭갈비", 820, "쌀밥", "콩나물국", "배추김치"),
                    dish("삼치구이", 720, "쌀밥", "된장국", "감자조림"),
                    dish("훈제오리", 860, "쌀밥", "무쌈", "쌈채소"),
                )
            ),
            Course(
                DINNER, "AB", HALL_CAMPUS_2, "도담찌개", listOf(
                    dish("참치김치찌개", 750, "쌀밥", "계란말이", "김구이"),
                    dish("순두부찌개", 700, "쌀밥", "어묵볶음", "깍두기"),
                )
            ),
            Course(
                DINNER, "AC", HALL_CAMPUS_2, "헬스기빙", listOf(
                    dish("두부스테이크", 520, "현미밥", "그린샐러드", "단호박찜"),
                    dish("닭가슴살 샐러드볼", 480, "군고구마", "요거트"),
                )
            ),
            Course(
                DINNER, "BA", HALL_CAMPUS_1, "봄이온소반", listOf(
                    dish("제육볶음", 840, "쌀밥", "미역국", "쌈채소"),
                    dish("해물파전", 780, "잔치국수", "배추김치"),
                )
            ),
        )
    }
}

package net.rivergod.sec.seoulrnd.android.menu.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.rivergod.sec.seoulrnd.android.menu.dto.DayCuisionsDTO
import java.time.LocalDate

class MenuRepository(private val source: MenuDataSource) {

    val isSample: Boolean get() = source.isSample

    suspend fun load(date: LocalDate): DayCuisionsDTO {
        val json = source.fetch(date)
        return withContext(Dispatchers.Default) { WelstoryMenuParser.parse(json, date) }
    }

    companion object {
        /**
         * true 로 바꾸면 0.9.14 의 웰스토리 API 를 호출한다.
         * 해당 API 가 동작하지 않아 기본값은 추정 데이터(SampleMenuDataSource) 이다.
         */
        const val USE_REMOTE = false

        fun create(): MenuRepository =
            MenuRepository(if (USE_REMOTE) WelstoryRemoteDataSource() else SampleMenuDataSource())
    }
}

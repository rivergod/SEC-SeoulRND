package net.rivergod.sec.seoulrnd.android.menu.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.rivergod.sec.seoulrnd.android.menu.BuildConfig
import net.rivergod.sec.seoulrnd.android.menu.dto.DayCuisionsDTO
import java.time.LocalDate

class MenuRepository(private val source: MenuDataSource) {

    val isSample: Boolean get() = source.isSample

    suspend fun load(date: LocalDate): DayCuisionsDTO {
        val json = source.fetch(date)
        return withContext(Dispatchers.Default) { WelstoryMenuParser.parse(json, date) }
    }

    companion object {
        /** 데이터 출처는 빌드 설정 seoulrnd.menuSource 로 정한다 (app/build.gradle.kts, 기본값 remote). */
        fun create(): MenuRepository =
            MenuRepository(if (BuildConfig.MENU_REMOTE) WelstoryRemoteDataSource() else SampleMenuDataSource())
    }
}

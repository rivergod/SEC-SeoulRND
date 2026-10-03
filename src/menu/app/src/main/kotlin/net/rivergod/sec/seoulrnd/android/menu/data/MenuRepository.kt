package net.rivergod.sec.seoulrnd.android.menu.data

import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.rivergod.sec.seoulrnd.android.menu.BuildConfig
import net.rivergod.sec.seoulrnd.android.menu.dto.DayCuisionsDTO
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

/** 두 식당 모두 메뉴를 불러오지 못한 경우. 원인은 첫 번째 실패를 담는다. */
class MenuLoadException(cause: Throwable) : Exception(cause.message, cause)

class MenuRepository(private val sources: List<MenuDataSource>) {

    val isSample: Boolean get() = sources.any { it.isSample }

    /**
     * 식당별로 동시에 불러온다. 한 식당이 실패해도 다른 식당의 메뉴는 돌려주고 실패한 식당을 표시한다.
     * @throws MenuLoadException 모든 식당이 실패한 경우
     */
    suspend fun load(date: LocalDate): DayCuisionsDTO = coroutineScope {
        val results = sources.map { source ->
            async {
                try {
                    Result.success(source.fetch(date))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "${source.cafeteria} menu load failed", e)
                    Result.failure(e)
                }
            }
        }.awaitAll()

        if (results.all { it.isFailure }) {
            throw MenuLoadException(results.first().exceptionOrNull()!!)
        }
        DayCuisionsDTO(
            date = date,
            cuisines = results.flatMap { it.getOrDefault(emptyList()) },
            failedCafeterias = sources.filterIndexed { i, _ -> results[i].isFailure }.map { it.cafeteria }.toSet(),
        )
    }

    companion object {
        private const val TAG = "MenuRepository"

        /** 데이터 출처는 빌드 설정 seoulrnd.menuSource 로 정한다 (app/build.gradle.kts, 기본값 remote). */
        fun create(): MenuRepository = MenuRepository(
            if (BuildConfig.MENU_REMOTE) {
                listOf(PulmuoneRemoteDataSource(), WelstoryRemoteDataSource())
            } else {
                SampleMenuDataSource.all()
            }
        )
    }
}

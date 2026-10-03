package net.rivergod.sec.seoulrnd.android.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO
import net.rivergod.sec.seoulrnd.android.menu.dto.MealType
import net.rivergod.sec.seoulrnd.android.menu.dto.MenuArea
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.MenuCalorieText
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.MenuNameText
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.MenuSideText
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.SectionHeaderBackground

/**
 * 캠퍼스 헤더 + 2열 메뉴 카드 목록.
 * item_bg 이미지(600x355)와 menu_item.xml 의 높이 배분이 2열 카드 기준으로 그려져 있어 2열로 배치한다.
 */
@Composable
fun MenuGrid(sections: List<MenuSection>, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 1.dp),
    ) {
        sections.forEach { section ->
            item(key = "header-${section.area}", span = { GridItemSpan(maxLineSpan) }) {
                MenuHeaderItem(section.area.label)
            }
            items(section.items, key = { it.id }) { cuisine ->
                MenuItemCard(cuisine)
            }
        }
    }
}

@Composable
fun MenuHeaderItem(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .background(SectionHeaderBackground)
            .padding(10.dp),
        color = MenuNameText,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp
    )
}

@Composable
fun MenuItemCard(cuisine: CuisineDTO, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(5.dp)
            .fillMaxWidth()
            .height(100.dp)
    ) {
        // item_bg: 위 흰색(아이콘) / 가운데 회색(메뉴명, 곁들임) / 아래 주황색(칼로리) 3단
        Image(
            painter = painterResource(R.drawable.item_bg),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(width = 80.dp, height = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (cuisine.iconRes != 0) {
                    Image(
                        painter = painterResource(cuisine.iconRes),
                        contentDescription = cuisine.courseName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = cuisine.courseName,
                        color = MenuNameText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            val title = cuisine.displayTitle
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, start = 5.dp, end = 5.dp)
                    .fillMaxWidth()
                    .height(23.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = MenuNameText,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (title.length > 13) 10.sp else 13.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = cuisine.sideDishes.joinToString(", "),
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .height(27.dp),
                color = MenuSideText,
                fontSize = 7.5.sp,
                lineHeight = 9.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = cuisine.displayCalorie,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                color = MenuCalorieText,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MenuMessage(message: String, modifier: Modifier = Modifier, content: @Composable () -> Unit = {}) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = MenuSideText, fontSize = 13.sp, textAlign = TextAlign.Center)
        content()
    }
}

@Preview(widthDp = 360)
@Composable
private fun MenuGridPreview() {
    fun cuisine(id: String, course: String, title: String, area: MenuArea, kcal: String = "760") = CuisineDTO(
        id = id,
        mealType = MealType.LUNCH,
        area = area,
        courseName = course,
        iconRes = MenuItemIconResource.getMenuIcon(course),
        title = title,
        sideDishes = listOf("쌀밥", "계란찜", "콩나물무침", "깍두기"),
        calorie = kcal,
    )
    MenuGrid(
        sections = listOf(
            MenuSection(
                MenuArea.DINE_IN,
                listOf(
                    cuisine("1", "도담찌개", "[맛집다녀왔습니다] 이북식닭개장", MenuArea.DINE_IN),
                    cuisine("2", "가츠앤", "등심돈까스(950kcal)", MenuArea.DINE_IN),
                    cuisine("3", "가든세이지", "홍합토마토파스타", MenuArea.DINE_IN),
                )
            ),
            MenuSection(MenuArea.TAKE_OUT, listOf(cuisine("4", "T/O 김밥/말이", "김밥[2~3Coin]", MenuArea.TAKE_OUT, "1346.44"))),
        )
    )
}

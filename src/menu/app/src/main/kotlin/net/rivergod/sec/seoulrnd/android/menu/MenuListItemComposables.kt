package net.rivergod.sec.seoulrnd.android.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.rivergod.sec.seoulrnd.android.menu.dto.Cafeteria
import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO
import net.rivergod.sec.seoulrnd.android.menu.dto.MealType
import net.rivergod.sec.seoulrnd.android.menu.dto.MenuArea
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.LocalBrandColors
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.SecSeoulRnDMenuTheme

/**
 * 식당별 메뉴 카드 그리드. 폭에 따라 열 수가 바뀐다(폰 2열).
 * Take Out 은 상품 목록이 길어 식당 코너를 가리지 않도록 기본으로 접어 둔다.
 */
@Composable
fun MenuGrid(
    sections: List<MenuSection>,
    onCuisineClick: (CuisineDTO) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onTakeOutToggled: (Cafeteria, expanded: Boolean) -> Unit = { _, _ -> },
) {
    var expandedTakeOut by rememberSaveable { mutableStateOf(emptyList<String>()) }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        var lastCafeteria: Cafeteria? = null
        sections.forEach { section ->
            if (section.cafeteria != lastCafeteria) {
                lastCafeteria = section.cafeteria
                item(key = "cafeteria-${section.cafeteria}", span = { GridItemSpan(maxLineSpan) }) {
                    CafeteriaHeader(section.cafeteria)
                }
            }
            when {
                section.failed -> item(key = "failed-${section.cafeteria}", span = { GridItemSpan(maxLineSpan) }) {
                    FailedCafeteria(onRetry)
                }

                section.area == MenuArea.TAKE_OUT -> {
                    val key = section.cafeteria.name
                    val expanded = key in expandedTakeOut
                    item(key = "takeout-${section.cafeteria}", span = { GridItemSpan(maxLineSpan) }) {
                        TakeOutHeader(
                            count = section.items.size,
                            expanded = expanded,
                            onClick = {
                                expandedTakeOut = if (expanded) expandedTakeOut - key else expandedTakeOut + key
                                onTakeOutToggled(section.cafeteria, !expanded)
                            }
                        )
                    }
                    if (expanded) {
                        items(section.items, key = { it.id }) { MenuItemCard(it, onClick = { onCuisineClick(it) }) }
                    }
                }

                else -> items(section.items, key = { it.id }) { MenuItemCard(it, onClick = { onCuisineClick(it) }) }
            }
        }
    }
}

@Composable
private fun CafeteriaHeader(cafeteria: Cafeteria) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 20.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(width = 4.dp, height = 28.dp)
                .clip(CircleShape)
                .background(LocalBrandColors.current.of(cafeteria))
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(cafeteria.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                cafeteria.vendor,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TakeOutHeader(count: Int, expanded: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Take Out", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            Text(
                "${count}개 코너",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (expanded) "접기" else "펼치기",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FailedCafeteria(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "메뉴를 불러오지 못했습니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onRetry) { Text("다시 시도") }
    }
}

/** 코너 표시: 웰스토리 코너는 로고 이미지, 그 밖에는 식당 강조색 글자 배지. */
@Composable
fun CornerBadge(cuisine: CuisineDTO, modifier: Modifier = Modifier) {
    if (cuisine.iconRes != 0) {
        // 로고 이미지는 흰 배경이라 다크 모드에서도 흰 바탕 위에 둔다
        Box(
            modifier = modifier
                .height(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Image(
                painter = painterResource(cuisine.iconRes),
                contentDescription = cuisine.courseName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(18.dp)
                    .aspectRatio(4f)
            )
        }
    } else {
        val accent = LocalBrandColors.current.of(cuisine.cafeteria)
        Box(
            modifier = modifier
                .height(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(accent.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                cuisine.courseName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MenuItemCard(cuisine: CuisineDTO, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = LocalBrandColors.current.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            CornerBadge(cuisine)
            Spacer(Modifier.height(10.dp))
            // 같은 줄의 카드 높이가 맞도록 줄 수를 고정한다
            Text(
                cuisine.displayTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                cuisine.sideDishes.joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                minLines = 3,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                cuisine.displayCalorie.ifEmpty { " " },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

/** 카드를 누르면 곁들임 메뉴 전체를 보여준다 (T/O 상품 목록은 수십 개라 카드에 다 담기지 않는다). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuisineDetailSheet(cuisine: CuisineDTO, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    ) {
        Column(
            Modifier
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CornerBadge(cuisine)
                Spacer(Modifier.width(8.dp))
                Text(
                    "${cuisine.cafeteria.shortLabel} · ${cuisine.courseName}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(cuisine.displayTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (cuisine.displayCalorie.isNotEmpty()) {
                Text(
                    cuisine.displayCalorie,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            if (cuisine.sideDishes.isEmpty()) {
                Text(
                    "구성 메뉴 정보가 없습니다.",
                    modifier = Modifier.padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
                    items(cuisine.sideDishes) { item ->
                        Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(LocalBrandColors.current.of(cuisine.cafeteria))
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(item, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 불러오는 중·오류·빈 목록 같은 전체 화면 안내.
 * 당겨서 새로고침이 동작하도록 세로 스크롤 가능한 영역으로 만들고, 내용은 화면 가운데에 둔다.
 */
@Composable
fun MenuMessage(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    content: @Composable () -> Unit = {},
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .heightIn(min = maxHeight)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MenuMessageContent(message, icon, content)
        }
    }
}

@Composable
private fun MenuMessageContent(message: String, icon: ImageVector?, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(16.dp))
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        content()
    }
}

@Preview(widthDp = 380, heightDp = 760)
@Preview(widthDp = 380, heightDp = 760, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MenuGridPreview() {
    fun cuisine(id: String, cafeteria: Cafeteria, course: String, title: String, area: MenuArea, kcal: String = "760") =
        CuisineDTO(
            id = id,
            cafeteria = cafeteria,
            mealType = MealType.LUNCH,
            area = area,
            courseName = course,
            iconRes = if (cafeteria == Cafeteria.CAFETERIA_2) MenuItemIconResource.getMenuIcon(course) else 0,
            title = title,
            sideDishes = listOf("쌀밥", "계란찜", "콩나물무침", "깍두기"),
            calorie = kcal,
        )
    SecSeoulRnDMenuTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            MenuGrid(
                sections = listOf(
                    MenuSection(
                        Cafeteria.CAFETERIA_2, MenuArea.DINE_IN,
                        listOf(
                            cuisine("1", Cafeteria.CAFETERIA_2, "도담찌개", "[맛집다녀왔습니다] 이북식닭개장", MenuArea.DINE_IN),
                            cuisine("2", Cafeteria.CAFETERIA_2, "가츠앤", "등심돈까스(950kcal)", MenuArea.DINE_IN),
                            cuisine("3", Cafeteria.CAFETERIA_2, "가든세이지", "홍합토마토파스타", MenuArea.DINE_IN),
                        )
                    ),
                    MenuSection(
                        Cafeteria.CAFETERIA_2, MenuArea.TAKE_OUT,
                        listOf(cuisine("4", Cafeteria.CAFETERIA_2, "T/O 김밥/말이", "김밥[2~3Coin]", MenuArea.TAKE_OUT, "1346.44"))
                    ),
                    MenuSection(
                        Cafeteria.CAFETERIA_1, MenuArea.DINE_IN,
                        listOf(cuisine("5", Cafeteria.CAFETERIA_1, "88℃온도", "미나리닭곰탕", MenuArea.DINE_IN, "1063"))
                    ),
                ),
                onCuisineClick = {},
                onRetry = {},
            )
        }
    }
}

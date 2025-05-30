package net.rivergod.sec.seoulrnd.android.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.rivergod.sec.seoulrnd.android.menu.dto.CuisineDTO

// Assuming R.drawable.item_bg is a color or simple drawable.
// For simplicity, using a light gray background as a placeholder.
// In a real app, this would be Modifier.background(painterResource(id = R.drawable.item_bg))
// or a more complex drawing.
val itemBgModifier = Modifier.background(Color(0xFFF0F0F0)) // Placeholder for @drawable/item_bg

@Composable
fun MenuHeaderItem(campusName: String) {
    Text(
        text = campusName,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x77a6e5ff)) // #77a6e5ff
            .padding(10.dp),
        color = Color(0xFF2c236d), // #2c236d
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp
    )
}

@Composable
fun MenuRowItem(cuisine: CuisineDTO) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp) // as per menu_item.xml
            .padding(5.dp) // margin in XML
            .then(itemBgModifier), // Applying placeholder background
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon: cuisine.cafeteriaCode is assumed to be a drawable resource ID
        // If it's 0 or invalid, painterResource might throw an error.
        // The original adapter directly uses it in setBackgroundResource.
        // For Jetpack Compose Image, it should be a valid drawable.
        // MenuItemIconResource.getMenuIcon(cuisine.cafeteriaUrl) might be safer if cafeteriaCode is not a drawable.
        // For now, sticking to the adapter's direct use of cuisine.getCafeteriaCode().
        val iconResId = cuisine.cafeteriaCode ?: R.drawable.dishes // Fallback to a default icon
        if (iconResId != 0) { // Check for a potentially valid resource ID
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = cuisine.title ?: "Menu icon",
                modifier = Modifier
                    .width(80.dp)
                    .height(20.dp)
                    .padding(top = 2.dp),
                contentScale = ContentScale.Fit // Or other appropriate scale
            )
        } else {
            // Placeholder if no valid icon resource ID is found
            Spacer(modifier = Modifier.height(20.dp).padding(top = 2.dp))
        }


        val title = cuisine.title?.let {
            if (it.contains("l)")) it.substring(0, it.indexOf("(")) else it
        } ?: ""

        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .height(23.dp)
                .padding(top = 4.dp, start = 5.dp, end = 5.dp),
            textAlign = TextAlign.Center,
            color = Color(0xFF2c236d), // #2c236d
            fontWeight = FontWeight.Bold,
            fontSize = if (title.length > 13) 10.sp else 13.sp
        )

        val content = cuisine.content ?: ""
        val lastIndexComma = content.lastIndexOf(",")
        val sideText = if (lastIndexComma != -1) content.substring(0, lastIndexComma) else ""

        Text(
            text = sideText,
            modifier = Modifier
                .fillMaxWidth()
                .height(27.dp)
                .padding(start = 10.dp, end = 10.dp),
            color = Color(0xFF303030), // #303030
            fontSize = 7.5.sp // This is a very small font size, ensure it's intended
        )

        Text(
            text = cuisine.calorie ?: "",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Takes remaining space in height
                .padding(top = 4.dp),
            textAlign = TextAlign.Center,
            color = Color.Red, // #FF0000
            fontSize = 11.sp
        )
    }
}

// Stub R class for R.drawable.dishes - this would be in the actual project's R file.
// object R {
//    object drawable {
//        const val dishes = 0 // Replace with a real drawable resource ID if testing in isolation
//        // const val item_bg = 0 // Placeholder for actual item_bg drawable
//    }
// }
// CuisineDTO would be imported from its definition
// package net.rivergod.sec.seoulrnd.android.menu.dto
// data class CuisineDTO(
//     var mealCode: Int?,
//     var campusCode: Int?,
//     var cafeteriaCode: Int?, // This is used as Icon Resource ID
//     var cafeteriaUrl: String?,
//     var title: String?,
//     var content: String?,
//     var calorie: String?
// ) {
//     companion object {
//         const val MEALCODE_BREAKFAST = 0
//         const val MEALCODE_LAUNCH = 1
//         const val MEALCODE_DINNER = 2
//         const val CAMPUSCODE_1 = 0
//         const val CAMPUSCODE_2 = 1
//     }
// }

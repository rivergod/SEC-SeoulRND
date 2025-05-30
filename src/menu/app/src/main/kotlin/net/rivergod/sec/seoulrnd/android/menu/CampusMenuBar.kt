package net.rivergod.sec.seoulrnd.android.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Assuming these colors and drawable would be defined in the project's resources.
// Placeholder values:
val selectTextColor = Color(0xFF007AFF) // A typical blue for selected text
val unselectTextColor = Color.Gray
// R.drawable.round_rect_line_blue_bottom would be a shape drawable.
// We can approximate with a bottom border.

@Composable
fun CampusMenuBar(
    campusNames: List<String>,
    selectedCampus: String,
    onCampusSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White) // Assuming a default background for the bar
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround, // Or Arrangement.Start
        verticalAlignment = Alignment.CenterVertically
    ) {
        campusNames.forEach { campusName ->
            val isSelected = campusName == selectedCampus
            val textColor = if (isSelected) selectTextColor else unselectTextColor
            
            // Modifier for the bottom line if selected
            val itemModifier = Modifier
                .weight(1f) // Give equal weight to each item if SpaceAround is used
                .padding(vertical = 8.dp)
                .clickable { onCampusSelected(campusName) }

            val bottomBorderModifier = if (isSelected) {
                // Approximating R.drawable.round_rect_line_blue_bottom
                // This would typically be a more complex shape drawable.
                // Here, we use a simple bottom border.
                Modifier.border(
                    width = 2.dp,
                    color = selectTextColor, // Using selectTextColor for the line color
                    shape = RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp) // Slight rounding if needed
                )
                // If it's a background drawable, it would be:
                // .background(color = selectTextColor, shape = ...)
            } else {
                Modifier
            }

            Text(
                text = campusName,
                modifier = itemModifier.then(bottomBorderModifier).padding(bottom = if(isSelected) 0.dp else 2.dp), // adjust padding to align text if border takes space
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Example Usage (for testing or preview, not part of the file usually)
/*
@Preview(showBackground = true)
@Composable
fun PreviewCampusMenuBar() {
    val campuses = listOf("Campus A", "Campus B", "Campus C")
    var selected by remember { mutableStateOf(campuses.first()) }
    CampusMenuBar(
        campusNames = campuses,
        selectedCampus = selected,
        onCampusSelected = { selected = it }
    )
}
*/

// Stubs for resources if needed for isolated preview/compilation
// object R {
//     object color {
//         val select_text_color = 0 // Placeholder
//         val unselect_text_color = 0 // Placeholder
//     }
//     object drawable {
//         val round_rect_line_blue_bottom = 0 // Placeholder
//     }
// }

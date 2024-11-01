package net.rivergod.sec.seoulrnd.android.menu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.rivergod.sec.seoulrnd.android.menu.ui.theme.SecSeoulRnDMenuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SecSeoulRnDMenuTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Main() {
//    Scaffold (
//        topBar = {
//            Surface {
//                TopAppBar(title = { /*TODO*/ })
//            }
//        }
//    ) {
//
//    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    SecSeoulRnDMenuTheme {
        Main()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SecSeoulRnDMenuTheme {
        Greeting("Android")
    }
}

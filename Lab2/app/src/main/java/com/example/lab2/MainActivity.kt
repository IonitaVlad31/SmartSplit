package com.example.lab2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.lab2.ui.theme.Lab2Theme

enum class Colors(id: Int, nameResId: Int) {
    RED(id = 0, nameResId = R.string.red),
    YELLOW(id = 1, nameResId = R.string.yellow),
    GREEN(id = 2, nameResId = R.string.green)
}

object ClassUser {
    private val name: String = "John"
    public var name2: String? = null
    val nameL: String by lazy {
        "Lazy Name"
    }
    lateinit var nameI: String

    fun playWithNames() {
        name = "John"
        name2?.let {
            name2 = "John"
        }
        nameL.length
        nameI.length
    }
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.e("MainActivity", "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.e("MainActivity", "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.e("MainActivity", "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.e("MainActivity", "onStop called")
    }

    override fun onRestart() {
        super.onRestart()
        Log.e("MainActivity", "onRestart called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("MainActivity", "onDestroy called")
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
    Lab2Theme {
        Greeting("Android")
    }
}
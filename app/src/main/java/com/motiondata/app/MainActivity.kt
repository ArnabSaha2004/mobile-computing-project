package com.motiondata.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.motiondata.app.ui.navigation.AppNavigation
import com.motiondata.app.ui.theme.CharcoalBackground
import com.motiondata.app.ui.theme.MotionDataTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MotionDataTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CharcoalBackground
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

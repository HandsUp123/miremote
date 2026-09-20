package com.miir.remote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.miir.remote.ui.nav.MiNavHost
import com.miir.remote.ui.theme.MiIrRemoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiIrRemoteTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val container = (application as MiIrApplication).container
                    MiNavHost(container)
                }
            }
        }
    }
}

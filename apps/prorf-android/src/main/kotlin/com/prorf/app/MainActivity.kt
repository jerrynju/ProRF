package com.prorf.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.prorf.app.ui.ProRfNavHost
import com.prorf.app.ui.theme.LocalProRfTheme
import com.prorf.app.ui.theme.ProRfTheme
import com.prorf.app.ui.theme.ProRfThemeState

/**
 * L4 App Shell — single-activity host.
 * Responsible for: edge-to-edge, theme, navigation root.
 * No business logic here.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(isSystemInDarkTheme()) }
            CompositionLocalProvider(
                LocalProRfTheme provides ProRfThemeState(
                    isDarkTheme = isDarkTheme,
                    toggleTheme = { isDarkTheme = !isDarkTheme },
                ),
            ) {
                ProRfTheme(darkTheme = isDarkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        ProRfNavHost()
                    }
                }
            }
        }
    }
}

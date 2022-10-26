package com.karthek.android.s.gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.karthek.android.s.gallery.c.ui.screens.ManualWorkTriggerScreen
import com.karthek.android.s.gallery.c.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManualWorkTriggerActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
		setContent { ScreenContent() }
	}

	@Composable
	fun ScreenContent() {
		AppTheme {
			Surface(
				modifier = Modifier.fillMaxSize(),
				color = MaterialTheme.colorScheme.background
			) {
				ManualWorkTriggerScreen()
			}
		}
	}
}

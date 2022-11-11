package com.karthek.android.s.gallery.c

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.Coil
import coil.ImageLoader
import com.karthek.android.s.gallery.c.helpers.SMediaIconFetcher
import com.karthek.android.s.gallery.c.helpers.SMediaIconKeyer
import com.karthek.android.s.gallery.c.ui.screens.MainScreen
import com.karthek.android.s.gallery.c.ui.theme.AppTheme
import com.karthek.android.s.gallery.workers.CLASSIFY_WORK_NAME
import com.karthek.android.s.gallery.workers.ClassifySMediaWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
		Coil.setImageLoader {
			ImageLoader.Builder(this)
				.components {
					add(SMediaIconKeyer())
					add(SMediaIconFetcher.Factory())
				}
				.build()
		}
		schedulePeriodicWorkers()
		setContent { ScreenContent() }
	}

	@Composable
	fun ScreenContent() {
		AppTheme {
			MainScreen()
		}
	}

	private fun schedulePeriodicWorkers() {
		var constraintsBuilder = Constraints.Builder()
			.setRequiresBatteryNotLow(true)
			.setRequiresStorageNotLow(true)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			constraintsBuilder = constraintsBuilder.setRequiresDeviceIdle(true)
		}
		val classifyWorkRequest =
			PeriodicWorkRequestBuilder<ClassifySMediaWorker>(1, TimeUnit.DAYS)
				.setConstraints(constraintsBuilder.build())
				.build()
		WorkManager.getInstance(applicationContext).apply {
			enqueueUniquePeriodicWork(
				CLASSIFY_WORK_NAME,
				ExistingPeriodicWorkPolicy.KEEP,
				classifyWorkRequest
			)
		}
	}
}
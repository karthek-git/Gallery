package com.karthek.android.s.gallery.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.c.state.SMediaAccess
import com.karthek.android.s.gallery.helper.createBitmap
import com.karthek.android.s.gallery.model.Classify
import com.karthek.android.s.gallery.state.db.SMedia
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class ClassifySMediaWorker @AssistedInject constructor(
	@Assisted private val appContext: Context,
	@Assisted params: WorkerParameters,
	private val repo: SMediaAccess
) :
	CoroutineWorker(appContext, params) {

	private val notificationManager =
		appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

	override suspend fun doWork(): Result {
		withContext(Dispatchers.IO) {
			createDB()
		}
		return Result.success()
	}

	private suspend fun setForegroundInfo(progress: Float) {
		setProgress(workDataOf(PROGRESS_TAG to progress))
		val cancel = applicationContext.getString(android.R.string.cancel)
		val intent = WorkManager.getInstance(applicationContext)
			.createCancelPendingIntent(id)

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createNotificationChannel()
		}

		val notification =
			NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID_CLASSIFY)
				.setSmallIcon(R.drawable.ic_launcher_foreground)
				.setContentTitle("Classifying...")
				.setOngoing(true)
				.addAction(android.R.drawable.ic_delete, cancel, intent)
				.setCategory(NotificationCompat.CATEGORY_PROGRESS)
				.setProgress(100, (progress * 100).toInt(), false)
				.build()

		setForeground(ForegroundInfo(NOTIFICATION_ID_CLASSIFY, notification))
	}

	@RequiresApi(Build.VERSION_CODES.O)
	private fun createNotificationChannel() {
		val channel =
			NotificationChannel(
				NOTIFICATION_CHANNEL_ID_CLASSIFY,
				"Classify progress",
				NotificationManager.IMPORTANCE_LOW
			).apply {
				description = "Shows classify progress notifications"
			}
		notificationManager.createNotificationChannel(channel)
	}

	private fun getBitmap(sMedia: SMedia): Bitmap? {
		return createBitmap(sMedia, 224, 224, appContext.contentResolver)
	}

	private suspend fun createDB() {
		val classify = Classify(appContext)
		val list: List<SMedia> = repo.getSMedia(null)
		Log.v("db", "started")
		for (i in 0..list.size) {
			val bitmap = getBitmap(list[i]) ?: continue
			list[i].cat = classify.getCategory(bitmap)
			repo.insertSMedia(list[i])
			setForegroundInfo(progress = i / list.size.toFloat())
		}
		Log.v("db", "ended")
	}
}

const val PROGRESS_TAG = "progress"
const val NOTIFICATION_CHANNEL_ID_CLASSIFY = "flash"
const val NOTIFICATION_ID_CLASSIFY = 1
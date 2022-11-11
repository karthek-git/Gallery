package com.karthek.android.s.gallery.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.karthek.android.s.gallery.R

abstract class CommonWorker(val appContext: Context, workerParameters: WorkerParameters) :
	CoroutineWorker(appContext, workerParameters) {

	protected abstract val notificationChannelId: String
	protected abstract val notificationChannelName: String
	protected abstract val notificationChannelDesc: String
	protected abstract val notificationId: Int
	protected abstract val notificationTitle: String

	val notificationManager =
		appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

	suspend fun setForegroundInfo(progress: Float) {
		setProgress(workDataOf(PROGRESS_TAG to progress))
		val cancel = applicationContext.getString(android.R.string.cancel)
		val intent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createNotificationChannel(notificationChannelId,
				notificationChannelName,
				notificationChannelDesc
			)
		}

		val notification = NotificationCompat.Builder(applicationContext, notificationChannelId)
			.setSmallIcon(R.drawable.ic_default_notify)
			.setContentTitle(notificationTitle)
			.setTicker(notificationTitle)
			.setOngoing(true)
			.addAction(android.R.drawable.ic_delete, cancel, intent)
			.setCategory(NotificationCompat.CATEGORY_PROGRESS)
			.setProgress(100, (progress * 100).toInt(), false).build()

		setForeground(ForegroundInfo(notificationId, notification))
	}

	@RequiresApi(Build.VERSION_CODES.O)
	private fun createNotificationChannel(id: String, name: String, description: String) {
		val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW).apply {
			this.description = description
		}
		notificationManager.createNotificationChannel(channel)
	}

}

const val PROGRESS_TAG = "progress"

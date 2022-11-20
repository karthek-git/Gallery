package com.karthek.android.s.gallery.workers

import android.content.Context
import android.graphics.Bitmap
import androidx.hilt.work.HiltWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.c.state.Prefs
import com.karthek.android.s.gallery.c.state.SMediaAccess
import com.karthek.android.s.gallery.helper.createBitmap
import com.karthek.android.s.gallery.model.CLASSIFY_ML_VERSION
import com.karthek.android.s.gallery.model.Classify
import com.karthek.android.s.gallery.model.FaceRecognizer
import com.karthek.android.s.gallery.state.db.SCategory
import com.karthek.android.s.gallery.state.db.SCategorySMediaCrossRef
import com.karthek.android.s.gallery.state.db.SMedia
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@HiltWorker
class ClassifySMediaWorker @AssistedInject constructor(
	@Assisted appContext: Context,
	@Assisted params: WorkerParameters,
	private val repo: SMediaAccess,
	private val prefs: Prefs,
	private val classify: Classify,
	private val faceRecognizer: FaceRecognizer,
) : CommonWorker(appContext, params) {

	override val notificationChannelId = "classify"
	override val notificationChannelName =
		appContext.getString(R.string.notify_channel_name_classify)
	override val notificationChannelDesc =
		appContext.getString(R.string.notify_channel_desc_classify)
	override val notificationId = 1
	override val notificationTitle = appContext.getString(R.string.notify_title_classify)

	override suspend fun doWork(): Result {
		withContext(Dispatchers.IO) {
			createDB()
		}
		notificationManager.cancel(notificationId)
		scheduleFaceClusterWork()
		return Result.success()
	}

	private fun scheduleFaceClusterWork() {
		val clusterFacesWorkRequest = OneTimeWorkRequestBuilder<ClusterFacesWorker>().build()
		WorkManager.getInstance(appContext).apply {
			enqueueUniqueWork(CLUSTER_FACES_WORK_NAME,
				ExistingWorkPolicy.REPLACE,
				clusterFacesWorkRequest)
		}
	}

	private fun getBitmap(sMedia: SMedia): Bitmap? {
		return createBitmap(sMedia, 260, 260, appContext.contentResolver)
	}

	private suspend fun getSMediaToProcess(): List<SMedia> {
		val currentPrefs = prefs.prefsFlow.first()
		val lastMLVersion = currentPrefs.lastMLVersion
		return if (CLASSIFY_ML_VERSION > lastMLVersion) {
			prefs.onLastMLVersionChange(CLASSIFY_ML_VERSION)
			insertSCategories()
			repo.getSMedia(sortAsc = true)
		} else {
			repo.getSMedia(fromDate = currentPrefs.lastDate, sortAsc = true)
		}
	}

	private suspend fun insertSCategories() {
		val sCategories = mutableListOf<SCategory>()
		appContext.assets.open("labels.txt").bufferedReader().useLines { lines ->
			lines.forEachIndexed { index, s -> sCategories.add(SCategory(index, s)) }
		}
		repo.insertSCategories(sCategories)
	}

	private suspend fun createDB() {
		val list = getSMediaToProcess()
		for (i in list.indices) {
			val bitmap = getBitmap(list[i]) ?: continue
			val classifyResult = classify.getCategory(bitmap)
			list[i].cat = classifyResult.first
			val sceneCategory = classifyResult.second
			repo.insertSCategoryWithSMedia(SCategorySMediaCrossRef(sceneCategory, list[i].id))
			list[i].faceEmbeddings = faceRecognizer.getFaceEmbeddings(bitmap)
			repo.insertSMedia(list[i])
			if ((i % 1000) == 0) prefs.onLastDateChange(list[i].date)
			setForegroundInfo(progress = i / list.size.toFloat())
		}
	}
}

const val CLASSIFY_WORK_NAME = "classifyWork"

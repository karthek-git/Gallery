package com.karthek.android.s.gallery.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.c.state.Prefs
import com.karthek.android.s.gallery.c.state.SMediaAccess
import com.karthek.android.s.gallery.ml.dbscan
import com.karthek.android.s.gallery.state.db.SFace
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class ClusterFacesWorker @AssistedInject constructor(
	@Assisted appContext: Context,
	@Assisted workerParameters: WorkerParameters,
	private val repo: SMediaAccess,
	private val prefs: Prefs,
) : CommonWorker(appContext, workerParameters) {

	override val notificationChannelId = "face_cluster"
	override val notificationChannelName =
		appContext.getString(R.string.notify_channel_name_face_cluster)
	override val notificationChannelDesc =
		appContext.getString(R.string.notify_channel_desc_face_cluster)
	override val notificationId = 2
	override val notificationTitle = appContext.getString(R.string.notify_title_face_cluster)

	override suspend fun doWork(): Result {
		withContext(Dispatchers.IO) {
			clusterFaces()
		}
		notificationManager.cancel(notificationId)
		return Result.success()
	}

	private suspend fun clusterFaces() {
		setForegroundInfo(0f)
		val x = repo.getLocalSMedia().flatMap { SMediaItem ->
			val embeddings = SMediaItem.faceEmbeddings ?: listOf()
			embeddings.map { embedding -> Pair(embedding, SMediaItem.id) }
		}
		setForegroundInfo(0.25f)
		val pair = dbscan(x, eps = 1f, minInstances = 1)
		setForegroundInfo(0.75f)
		repo.insertSFaces(Array(pair.second) { index -> SFace(index, "") })
		repo.insertSFaceWithSMedia(pair.first)
		setForegroundInfo(1f)
	}
}

const val CLUSTER_FACES_WORK_NAME = "clusterFacesWork"

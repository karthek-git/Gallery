package com.karthek.android.s.gallery.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.c.state.Prefs
import com.karthek.android.s.gallery.c.state.SMediaAccess
import com.karthek.android.s.gallery.ml.dbscan
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
		val x = repo.getSMedia(sortAsc = true).flatMap { SMediaItem ->
			val embeddings = SMediaItem.faceEmbeddings ?: listOf()
			embeddings.map { embedding -> Pair(embedding, SMediaItem.id) }
		}
		repo.insertSFaceWithSMedia(dbscan(x).first)
	}
}

const val CLUSTER_FACES_WORK_NAME = "clusterFacesWork"

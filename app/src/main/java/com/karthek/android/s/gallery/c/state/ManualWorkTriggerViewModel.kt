package com.karthek.android.s.gallery.c.state

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.karthek.android.s.gallery.workers.CLASSIFY_WORK_NAME
import com.karthek.android.s.gallery.workers.ClassifySMediaWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ManualWorkTriggerViewModel @Inject constructor(private val appContext: Application) :
	ViewModel() {

	val classifyProgress =
		WorkManager.getInstance(appContext).getWorkInfosForUniqueWorkLiveData(CLASSIFY_WORK_NAME)

	fun onTriggerClick() {
		val classifyWorkRequest = OneTimeWorkRequestBuilder<ClassifySMediaWorker>().build()
		WorkManager.getInstance(appContext).apply {
			enqueueUniqueWork(CLASSIFY_WORK_NAME, ExistingWorkPolicy.REPLACE, classifyWorkRequest)
		}
	}

	fun onSuccessConfirmed() {
		WorkManager.getInstance(appContext).pruneWork()
	}

}
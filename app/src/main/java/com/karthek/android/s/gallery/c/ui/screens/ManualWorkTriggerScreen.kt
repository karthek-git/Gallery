package com.karthek.android.s.gallery.c.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkInfo
import com.karthek.android.s.gallery.c.state.ManualWorkTriggerViewModel
import com.karthek.android.s.gallery.workers.PROGRESS_TAG

@Composable
fun ManualWorkTriggerScreen(viewModel: ManualWorkTriggerViewModel= viewModel()) {
	ManualWorkTriggerScreenContent(
		viewModel.classifyProgress,
		viewModel::onTriggerClick,
		viewModel::onSuccessConfirmed
	)
}

@Composable
fun ManualWorkTriggerScreenContent(
	workProgress: LiveData<MutableList<WorkInfo>>, onTriggerClick: () -> Unit, onConfirm: () -> Unit
) {
	Column(
		modifier = Modifier.fillMaxSize(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		val classifyWorkData by workProgress.observeAsState()
		val classifying = !(classifyWorkData.isNullOrEmpty())
		if (!classifying) {
			Text("Click TRIGGER to run classify work manually")
			Spacer(modifier = Modifier.height(32.dp))
			Button(onClick = onTriggerClick) {
				Text("TRIGGER")
			}
		}
		if (classifying) {
			ClassifyState(
				workInfo = classifyWorkData!![0],
				onConfirm = onConfirm,
				modifier = Modifier
					.padding(32.dp)
					.align(Alignment.CenterHorizontally)
			)
		}

	}
}

@Composable
fun ClassifyState(
	workInfo: WorkInfo,
	onConfirm: () -> Unit,
	modifier: Modifier
) {
	when (workInfo.state) {
		WorkInfo.State.SUCCEEDED -> {
			FlashSuccessDialog("Classification successfully", onConfirm)
		}
		WorkInfo.State.RUNNING -> {
			ClassifyProgress(
				progress = workInfo.progress.getFloat(PROGRESS_TAG, 0f),
				modifier = modifier
			)
		}
		WorkInfo.State.CANCELLED -> {
			FlashSuccessDialog(text = "Classification cancelled", onConfirm)
		}
		WorkInfo.State.FAILED -> {
			FlashSuccessDialog(text = "Failed to Classify", onConfirm)
		}
		else -> {
			Log.v("state", "is ${workInfo.state}")
		}
	}
}

@Composable
fun ClassifyProgress(progress: Float, modifier: Modifier) {
	Column(modifier = modifier) {
		Text(text = "Running Classify...", style = MaterialTheme.typography.labelMedium)
		LinearProgressIndicator(
			progress = progress, modifier = Modifier
				.padding(vertical = 16.dp)
				.height(8.dp)
				.clip(RoundedCornerShape(4.dp))
		)
	}
}

@Composable
fun FlashSuccessDialog(text: String, onConfirm: () -> Unit) {
	Dialog(onDismissRequest = onConfirm) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.background(
					color = MaterialTheme.colorScheme.surface
						.copy(0.88f)
						.compositeOver(MaterialTheme.colorScheme.onSurface),
					shape = RoundedCornerShape(16.dp)
				)
				.padding(horizontal = 2.dp)
		) {
			Text(
				text = text,
				style = MaterialTheme.typography.bodySmall,
				modifier = Modifier
					.padding(top = 32.dp, bottom = 16.dp)
			)
			TextButton(
				onClick = onConfirm, modifier = Modifier
					.fillMaxWidth()
					.padding(4.dp)
			) {
				Text(text = stringResource(id = android.R.string.ok))
			}
		}
	}
}
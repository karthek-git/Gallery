package com.karthek.android.s.gallery.c.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.karthek.android.s.gallery.BuildConfig


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Perms(content: @Composable () -> Unit) {
	val storagePermissionState =
		rememberPermissionState(android.Manifest.permission.READ_EXTERNAL_STORAGE)
	when (storagePermissionState.status) {
		PermissionStatus.Granted -> {
			content()
		}
		is PermissionStatus.Denied -> {
				PermissionNotGrantedContent(storagePermissionState)
		}
	}
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionNotGrantedContent(permissionState: PermissionState) {
	Column(
		modifier = Modifier.fillMaxSize(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text("Please grant the storage permission.")
		Spacer(modifier = Modifier.height(32.dp))
		Button(onClick = { permissionState.launchPermissionRequest() }) {
			Text(stringResource(android.R.string.ok))
		}
	}
}

@Composable
fun PermissionNotAvailableContent() {
	val context = LocalContext.current
	Column(
		modifier = Modifier.fillMaxSize(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text("Storage permission denied.")
		Text("Please, grant access on the Settings screen.")
		Spacer(modifier = Modifier.height(32.dp))
		Button(onClick = {
			context.startActivity(
				Intent(
					ACTION_APPLICATION_DETAILS_SETTINGS,
					Uri.parse("package:${BuildConfig.APPLICATION_ID}")
				)
			)
		}) {
			Text("Open Settings")
		}
	}
}
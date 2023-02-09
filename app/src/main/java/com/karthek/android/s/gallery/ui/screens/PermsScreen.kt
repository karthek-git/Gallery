package com.karthek.android.s.gallery.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.karthek.android.s.gallery.BuildConfig


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Perms(content: @Composable () -> Unit) {
	//todo better handle
	val storagePermissionState = rememberMultiplePermissionsState(getPermissions())

	if (storagePermissionState.allPermissionsGranted) {
		content()
	} else {
		PermissionNotGrantedContent(onClick = {
			storagePermissionState.launchMultiplePermissionRequest()
		})
	}
}


@Composable
fun PermissionNotGrantedContent(onClick: () -> Unit) {
	Column(
		modifier = Modifier.fillMaxSize(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text("Please grant the storage permission.")
		Spacer(modifier = Modifier.height(32.dp))
		Button(onClick = onClick) {
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

fun getPermissions(): List<String> {
	val requiredPermissions = mutableListOf<String>()
	when {
		Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2 -> {
			requiredPermissions.add(Manifest.permission.READ_MEDIA_IMAGES)
			requiredPermissions.add(Manifest.permission.READ_MEDIA_VIDEO)
		}
		Build.VERSION.SDK_INT > Build.VERSION_CODES.P -> {
			requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
		}
		else -> {
			requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
		}
	}
	return requiredPermissions
}
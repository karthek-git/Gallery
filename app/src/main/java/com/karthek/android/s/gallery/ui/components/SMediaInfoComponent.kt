package com.karthek.android.s.gallery.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.karthek.android.s.gallery.state.ImageInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SMediaInfoComponent(viewModel: ImageInfoViewModel, onBackClick: () -> Unit) {
	Scaffold(topBar = {
		TopAppBar(title = { Text(text = "Info") }, navigationIcon = {
			IconButton(onClick = onBackClick) {
				Icon(
					imageVector = Icons.Outlined.ArrowBack,
					contentDescription = ""
				)
			}
		})
	}) { contentPadding ->
		SMediaInfoComponentContent(
			viewModel = viewModel,
			modifier = Modifier.padding(contentPadding)
		)
	}
}

@Composable
fun SMediaInfoComponentContent(viewModel: ImageInfoViewModel, modifier: Modifier) {
	Column(modifier = modifier.verticalScroll(rememberScrollState())) {
		SMediaInfoItem(
			headLineText = viewModel.album,
			supportingText = viewModel.path,
			icon = Icons.Outlined.FilterNone
		)
		if (viewModel.hasExifMetaData) {
			SMediaInfoItem(
				headLineText = viewModel.takenDate,
				supportingText = viewModel.modifiedDate,
				icon = Icons.Outlined.Today
			)
		} else {
			SMediaInfoItem(headLineText = viewModel.modifiedDate, icon = Icons.Outlined.Today)
		}
		SMediaInfoItem(
			headLineText = viewModel.name,
			supportingText = viewModel.size,
			icon = Icons.Outlined.Image
		)
		if (viewModel.hasExifMetaData) {
			SMediaInfoItem(
				headLineText = viewModel.oem,
				supportingText = viewModel.params,
				icon = Icons.Outlined.Camera
			)
		}
		SMediaInfoItem(headLineText = viewModel.mimetype,
			style = MaterialTheme.typography.titleMedium,
			icon = Icons.Outlined.Info)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SMediaInfoItem(
	headLineText: String,
	supportingText: String,
	icon: ImageVector,
) {
	ListItem(
		headlineContent = { Text(text = headLineText, fontWeight = FontWeight.SemiBold) },
		supportingContent = {
			Text(
				text = supportingText,
				style = MaterialTheme.typography.bodyMedium
			)
		},
		leadingContent = { Icon(imageVector = icon, contentDescription = "") },
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SMediaInfoItem(
	headLineText: String,
	style: TextStyle = MaterialTheme.typography.bodyMedium,
	icon: ImageVector,
) {
	ListItem(
		headlineContent = {
			Text(text = headLineText, fontWeight = FontWeight.SemiBold, style = style)
		},
		leadingContent = { Icon(imageVector = icon, contentDescription = "") },
	)
}

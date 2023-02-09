package com.karthek.android.s.gallery.ui.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.state.ImageInfScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageInfScreen(
	viewModel: ImageInfScreenViewModel,
	selectImageClick: () -> Unit,
) {
	val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
	Scaffold(topBar = {
		TopAppBar(
			title = {
				Text(
					text = stringResource(id = R.string.app_name),
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			},
			scrollBehavior = scrollBehavior
		)
	}, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) { innerPadding ->
		Column(
			modifier = Modifier
				.padding(innerPadding)
				.padding(8.dp)
				.verticalScroll(rememberScrollState())
		) {
			Card(
				onClick = selectImageClick, modifier = Modifier
					.fillMaxWidth()
					.height(240.dp)
					.padding(8.dp)
			) {
				Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
					if (viewModel.imageUri == null) {

						Icon(
							imageVector = Icons.Outlined.AddAPhoto,
							contentDescription = "",
						)

					} else {
						viewModel.imgBitmap?.let { imgBitmap ->
							Image(
								painter = BitmapPainter(imgBitmap.asImageBitmap()),
								contentDescription = "",
								modifier = Modifier.fillMaxSize()
							)
						}

					}
					if (viewModel.infInProgress) {
						LinearProgressIndicator(
							modifier = Modifier
								.fillMaxWidth()
								.align(Alignment.BottomCenter)
						)
					}
				}
			}
			if (viewModel.imageUri != null) {
				Button(
					enabled = (!(viewModel.infInProgress)),
					onClick = viewModel::onRunClick,
					modifier = Modifier
						.padding(16.dp)
						.align(Alignment.CenterHorizontally)
				) {
					Text(text = "RUN")
				}

				Text(
					text = viewModel.infOutText,
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
				)
			}
		}
	}
}
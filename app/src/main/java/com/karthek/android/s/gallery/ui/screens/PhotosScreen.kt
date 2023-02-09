package com.karthek.android.s.gallery.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.state.SMViewModel
import com.karthek.android.s.gallery.state.SMediaStateList
import com.karthek.android.s.gallery.state.db.SMedia

@Composable
fun PhotosScreen(
	viewModel: SMViewModel,
	paddingValues: PaddingValues,
	callback: (i: Int) -> Unit,
) {
	PhotosScreenContent(sMediaStateList = viewModel.sMediaList, paddingValues, callback)
}

@Composable
fun PhotosScreenContent(
	sMediaStateList: SMediaStateList,
	paddingValues: PaddingValues = PaddingValues(),
	callback: (i: Int) -> Unit,
) {
	if (sMediaStateList.isLoading.value) {
		ContentLoading()
	} else if (sMediaStateList.list.isEmpty()) {
		ContentEmpty()
	} else {
		SMediaGrid(SMediaList = sMediaStateList.list, paddingValues, callback)
	}
}

@Composable
fun SMediaGrid(SMediaList: List<SMedia>, paddingValues: PaddingValues, callback: (i: Int) -> Unit) {
	LazyVerticalGrid(
		columns = GridCells.Adaptive(120.dp),
		contentPadding = paddingValues,
	) {
		itemsIndexed(SMediaList) { i, item ->
			SMediaItem(item, i, callback)
		}
	}
}


@Composable
fun SMediaItem(sMedia: SMedia, i: Int, callback: (i: Int) -> Unit) {
	Box {
		AsyncImage(
			model = ImageRequest.Builder(LocalContext.current)
				.data(sMedia)
				.build(),
			contentDescription = "",
			modifier = Modifier
				.size(120.dp)
				.padding(1.dp)
				.clickable { callback(i) },
			contentScale = ContentScale.Crop
		)
		if (sMedia.isVideo) {
			Icon(
				imageVector = Icons.Default.PlayArrow,
				contentDescription = stringResource(R.string.video),
				tint = Color.White,
				modifier = Modifier
					.align(Alignment.TopEnd)
					.size(24.dp)
					.padding(2.dp)
			)
		}
	}
}
package com.karthek.android.s.gallery.c.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.karthek.android.s.gallery.c.state.SMViewModel
import com.karthek.android.s.gallery.state.db.SMedia

@Composable
fun PhotosScreen(
	viewModel: SMViewModel,
	index: Int = -1,
	paddingValues: PaddingValues,
	callback: (i: Int) -> Unit
) {
	val list = if (index == -1) {
		viewModel.sMediaList
	} else {
		viewModel.getFolderContents(index).value?.get()
	}
	PhotosScreenContent(SMediaList = list, paddingValues, callback)
}

@Composable
fun PhotosScreenContent(
	SMediaList: List<SMedia>?,
	paddingValues: PaddingValues = PaddingValues(),
	callback: (i: Int) -> Unit
) {
	if (SMediaList == null) {
		ContentLoading()
	} else if(SMediaList.isEmpty()){
		ContentEmpty()
	}else {
		SMediaGrid(SMediaList = SMediaList, paddingValues, callback)
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
	AsyncImage(
		model = ImageRequest.Builder(LocalContext.current)
			.data(sMedia)
			.crossfade(true)
			.build(),
		contentDescription = "",
		modifier = Modifier
			.size(120.dp)
			.padding(1.dp)
			.clickable { callback(i) },
		contentScale = ContentScale.Crop
	)
}
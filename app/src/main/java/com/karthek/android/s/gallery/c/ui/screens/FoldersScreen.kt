package com.karthek.android.s.gallery.c.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.c.a.MFolder
import com.karthek.android.s.gallery.c.state.SMViewModel

@Composable
fun FoldersScreen(
	viewModel: SMViewModel,
	paddingValues: PaddingValues,
	callback: (i: Int) -> Unit,
) {
	FoldersScreenContent(viewModel.folderList, paddingValues, callback)
}

@Composable
fun FoldersScreenContent(
	folderList: List<MFolder>?,
	paddingValues: PaddingValues,
	callback: (i: Int) -> Unit,
) {
	if (folderList == null) {
		ContentLoading()
	} else {
		FoldersGrid(folderList, paddingValues, callback)
	}
}

@Composable
fun FoldersGrid(
	folderList: List<MFolder>,
	paddingValues: PaddingValues,
	callback: (i: Int) -> Unit,
) {
	LazyVerticalGrid(
		columns = GridCells.Adaptive(150.dp),
		contentPadding = paddingValues,
		modifier = Modifier.padding(horizontal = 16.dp)
	) {
		itemsIndexed(folderList) { i, c ->
			FolderItem(c, i, callback)
		}
	}
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FolderItem(folder: MFolder, index: Int, callback: (i: Int) -> Unit) {
	Column(modifier = Modifier.padding(4.dp)) {
		AsyncImage(
			model = ImageRequest.Builder(LocalContext.current)
				.data(folder.previewSMedia)
				.build(),
			contentDescription = folder.name,
			modifier = Modifier
				.size(150.dp)
				.padding(1.dp)
				.clip(RoundedCornerShape(10.dp))
				.background(MaterialTheme.colorScheme.onSurfaceVariant)
				.clickable { callback(index) },
			contentScale = ContentScale.Crop
		)
		Text(
			text = folder.name,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.padding(top = 4.dp),
			style = MaterialTheme.typography.labelLarge
		)
		Text(
			text = pluralStringResource(
				id = R.plurals.items,
				count = folder.numItems,
				folder.numItems),
			style = MaterialTheme.typography.labelSmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			fontWeight = FontWeight.Normal
		)
	}
}
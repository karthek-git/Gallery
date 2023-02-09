package com.karthek.android.s.gallery.ui.screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.state.FacesViewModel
import com.karthek.android.s.gallery.state.db.SFaceWithSMedia

@Composable
fun FacesScreen(
	viewModel: FacesViewModel,
	onItemClick: (i: Int) -> Unit,
	onBackClick: () -> Unit,
) {
	FacesScreenContent(viewModel.sFacesWithSMedia, onItemClick, onBackClick)
}

@Composable
fun FacesScreenContent(
	sFacesWithSMedia: List<SFaceWithSMedia>?,
	onItemClick: (i: Int) -> Unit,
	onBackClick: () -> Unit,
) {
	DestScaffold(
		name = stringResource(R.string.people),
		onBackClick = onBackClick
	) { paddingValues ->
		if (sFacesWithSMedia == null) {
			ContentLoading()
		} else {
			FacesGrid(
				sFacesWithSMedia = sFacesWithSMedia,
				paddingValues = paddingValues,
				callback = onItemClick
			)
		}
	}
}

@Composable
fun FacesGrid(
	sFacesWithSMedia: List<SFaceWithSMedia>,
	paddingValues: PaddingValues,
	callback: (i: Int) -> Unit,
) {
	LazyVerticalGrid(
		columns = GridCells.Adaptive(150.dp),
		contentPadding = paddingValues,
		modifier = Modifier.padding(horizontal = 16.dp)
	) {
		itemsIndexed(sFacesWithSMedia) { i, c ->
			FaceItem(c, i, callback)
		}
	}
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FaceItem(sFaceWithSMedia: SFaceWithSMedia, index: Int, callback: (i: Int) -> Unit) {
	Column(modifier = Modifier
		.padding(4.dp)
		.clickable { callback(index) }) {
		AsyncImage(
			model = ImageRequest.Builder(LocalContext.current)
				.data(sFaceWithSMedia.SMediaList.first())
				.build(),
			contentDescription = "",
			modifier = Modifier
				.size(150.dp)
				.padding(1.dp)
				.clip(RoundedCornerShape(10.dp)),
			contentScale = ContentScale.Crop
		)
		Text(
			text = sFaceWithSMedia.sFace.name,
			modifier = Modifier.padding(top = 4.dp),
			style = MaterialTheme.typography.labelLarge
		)
		Text(
			text = pluralStringResource(
				id = R.plurals.items,
				count = sFaceWithSMedia.SMediaList.size,
				sFaceWithSMedia.SMediaList.size),
			style = MaterialTheme.typography.labelSmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			fontWeight = FontWeight.Normal
		)
	}
}

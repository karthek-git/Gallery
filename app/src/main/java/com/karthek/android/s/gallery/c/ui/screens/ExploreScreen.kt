package com.karthek.android.s.gallery.c.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.c.state.CategoriesViewModel
import com.karthek.android.s.gallery.c.state.FacesViewModel
import com.karthek.android.s.gallery.c.state.SMViewModel
import com.karthek.android.s.gallery.c.ui.components.SearchTextField
import com.karthek.android.s.gallery.state.db.SCategoryWithSMedia
import com.karthek.android.s.gallery.state.db.SFaceWithSMedia
import com.karthek.android.s.gallery.state.db.SMedia

@Composable
fun ExploreScreen(
	viewModel: SMViewModel,
	paddingValues: PaddingValues,
	onSearchAction: (String) -> Unit,
	facesViewModel: FacesViewModel,
	onPeopleClick: () -> Unit,
	onFaceItemClick: (Int) -> Unit,
	categoriesViewModel: CategoriesViewModel,
	onThingsClick: () -> Unit,
	onThingItemClick: (Int) -> Unit,
) {
	ExploreScreenContent(
		paddingValues = paddingValues,
		showProgressBar = viewModel.searchInProgress,
		onSearchAction = { query ->
			viewModel.onSearchAction(query)
			onSearchAction(query)
		},
		sFacesWithSMedia = facesViewModel.sFacesWithSMedia,
		onPeopleClick = onPeopleClick,
		onFaceItemClick = onFaceItemClick,
		sCategoriesWithSMedia = categoriesViewModel.sCategoriesWithSMedia,
		onThingsClick = onThingsClick,
		onThingItemClick = onThingItemClick
	)
}


@Composable
fun ExploreScreenContent(
	paddingValues: PaddingValues,
	showProgressBar: Boolean,
	onSearchAction: (String) -> Unit,
	sFacesWithSMedia: List<SFaceWithSMedia>?,
	onPeopleClick: () -> Unit,
	onFaceItemClick: (Int) -> Unit,
	sCategoriesWithSMedia: List<SCategoryWithSMedia>?,
	onThingsClick: () -> Unit,
	onThingItemClick: (Int) -> Unit,
) {
	var textFieldValue by rememberSaveable { mutableStateOf("") }
	Surface(modifier = Modifier.padding(paddingValues)) {
		Column {
			SearchTextField(
				textFieldValue,
				onValueChange = { textFieldValue = it },
				onSearchAction = { onSearchAction(textFieldValue) })
			if (showProgressBar) {
				ContentLoading()
			}
			if (sFacesWithSMedia != null && sFacesWithSMedia.isNotEmpty()) {
				FacesRow(
					sFacesWithSMedia = sFacesWithSMedia,
					onPeopleClick = onPeopleClick,
					onFaceItemClick = onFaceItemClick
				)
			}
			if (sCategoriesWithSMedia != null && sCategoriesWithSMedia.isNotEmpty()) {
				CategoriesRow(
					sCategoriesWithSMedia = sCategoriesWithSMedia,
					onThingsClick = onThingsClick,
					onThingItemClick = onThingItemClick
				)
			}
		}
	}
}


@Composable
fun ExploreRowHeader(@StringRes id: Int, onMoreClick: () -> Unit) {
	Row(horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier.fillMaxWidth()) {
		Text(
			text = stringResource(id),
			modifier = Modifier.padding(8.dp),
			style = MaterialTheme.typography.titleMedium
		)
		IconButton(onClick = onMoreClick) {
			Icon(imageVector = Icons.Outlined.ArrowForward,
				contentDescription = stringResource(id = R.string.explore))
		}
	}
}


@Composable
fun FacesRow(
	sFacesWithSMedia: List<SFaceWithSMedia>,
	onPeopleClick: () -> Unit,
	onFaceItemClick: (Int) -> Unit,
) {
	Column(modifier = Modifier.padding(8.dp)) {
		ExploreRowHeader(id = R.string.people, onMoreClick = onPeopleClick)
		LazyRow {
			itemsIndexed(sFacesWithSMedia) { i, c ->
				FaceItem(sMedia = c.SMediaList.first(), index = i, callback = onFaceItemClick)
			}
		}
	}
}

@Composable
fun FaceItem(sMedia: SMedia, index: Int, callback: (i: Int) -> Unit) {
	Column(modifier = Modifier
		.padding(4.dp)
		.clickable { callback(index) }) {
		AsyncImage(
			model = ImageRequest.Builder(LocalContext.current)
				.data(sMedia)
				.build(),
			contentDescription = "",
			modifier = Modifier
				.size(90.dp)
				.padding(1.dp)
				.clip(CircleShape),
			contentScale = ContentScale.Crop
		)
	}
}

@Composable
fun CategoriesRow(
	sCategoriesWithSMedia: List<SCategoryWithSMedia>,
	onThingsClick: () -> Unit,
	onThingItemClick: (Int) -> Unit,
) {
	Column(modifier = Modifier.padding(8.dp)) {
		ExploreRowHeader(id = R.string.things, onMoreClick = onThingsClick)
		LazyRow {
			itemsIndexed(sCategoriesWithSMedia) { i, c ->
				CategoryItem(sCategoryWithSMedia = c, index = i, callback = onThingItemClick)
			}
		}
	}
}

@Composable
fun CategoryItem(sCategoryWithSMedia: SCategoryWithSMedia, index: Int, callback: (i: Int) -> Unit) {
	Column(modifier = Modifier
		.padding(8.dp)
	) {
		AsyncImage(
			model = ImageRequest.Builder(LocalContext.current)
				.data(sCategoryWithSMedia.SMediaList.first())
				.build(),
			contentDescription = "",
			modifier = Modifier
				.size(132.dp)
				.padding(1.dp)
				.clip(RoundedCornerShape(16.dp))
				.clickable { callback(index) },
			contentScale = ContentScale.Crop
		)
		Text(text = sCategoryWithSMedia.sCategory.name,
			color = Color.White,
			style = MaterialTheme.typography.labelLarge,
			modifier = Modifier
				.align(Alignment.CenterHorizontally)
				.padding(4.dp)
		)
	}
}

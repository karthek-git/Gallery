package com.karthek.android.s.gallery.c.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.c.state.SMViewModel
import com.karthek.android.s.gallery.c.ui.components.SearchTextField
import com.karthek.android.s.gallery.state.db.SMedia

@Composable
fun ExploreScreen(
	viewModel: SMViewModel,
	paddingValues: PaddingValues,
	onItemClick: (Int) -> Unit,
	onPeopleClick: () -> Unit,
) {
	ExploreScreenContent(
		paddingValues = paddingValues,
		showProgressBar = viewModel.searchInProgress,
		sMediaList = viewModel.searchResultSMedia,
		onSearchAction = viewModel::onSearchAction,
		onItemClick = onItemClick,
		onPeopleClick = onPeopleClick
	)
}


@Composable
fun ExploreScreenContent(
	paddingValues: PaddingValues,
	showProgressBar: Boolean,
	sMediaList: List<SMedia>?,
	onSearchAction: (String) -> Unit,
	onItemClick: (Int) -> Unit,
	onPeopleClick: () -> Unit,
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
			if (!showProgressBar && sMediaList != null) {
				PhotosScreenContent(SMediaList = sMediaList, callback = onItemClick)
			} else {
				FacesRow(onPeopleClick)
			}
		}
	}
}

@Composable
fun FacesRow(onPeopleClick: () -> Unit) {
	Column(modifier = Modifier
		.padding(4.dp)
		.clickable { onPeopleClick() }) {
		Icon(
			imageVector = Icons.Outlined.People,
			contentDescription = "",
			modifier = Modifier
				.size(150.dp)
				.padding(1.dp)
				.clip(RoundedCornerShape(10.dp)),
		)
		Text(
			text = stringResource(id = R.string.people),
			modifier = Modifier.align(Alignment.CenterHorizontally),
			style = MaterialTheme.typography.labelLarge
		)
	}
}
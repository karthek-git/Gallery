package com.karthek.android.s.gallery.c.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.karthek.android.s.gallery.c.state.SMViewModel
import com.karthek.android.s.gallery.c.ui.components.SearchTextField
import com.karthek.android.s.gallery.state.db.SMedia

@Composable
fun ExploreScreen(
	viewModel: SMViewModel,
	paddingValues: PaddingValues,
	onItemClick: (Int) -> Unit
) {
	ExploreScreenContent(
		paddingValues = paddingValues,
		showProgressBar = viewModel.searchInProgress,
		sMediaList = viewModel.searchResultSMedia,
		onSearchAction = viewModel::onSearchAction,
		onItemClick = onItemClick
	)
}


@Composable
fun ExploreScreenContent(
	paddingValues: PaddingValues,
	showProgressBar: Boolean,
	sMediaList: List<SMedia>?,
	onSearchAction: (String) -> Unit,
	onItemClick: (Int) -> Unit
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
			}
		}
	}
}
package com.karthek.android.s.gallery.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.state.SMViewModel

@Composable
fun DestScreen(
	title: String,
	viewModel: SMViewModel,
	onBackClick: () -> Unit,
	onItemClick: (Int) -> Unit,
) {
	DestScaffold(name = title, onBackClick = onBackClick) { paddingValues ->
		PhotosScreenContent(viewModel.currentSMediaList, paddingValues, onItemClick)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestScaffold(
	name: String,
	onBackClick: () -> Unit,
	content: @Composable (PaddingValues) -> Unit,
) {
	val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(text = name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
				navigationIcon = {
					IconButton(onClick = onBackClick) {
						Icon(
							imageVector = Icons.Outlined.ArrowBack,
							contentDescription = stringResource(id = R.string.go_back)
						)
					}
				},
				scrollBehavior = scrollBehavior
			)
		}, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
	) {
		content(it)
	}
}

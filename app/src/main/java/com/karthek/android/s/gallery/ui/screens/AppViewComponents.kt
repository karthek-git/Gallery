package com.karthek.android.s.gallery.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ContentLoading() {
	CircularProgressIndicator(
		modifier = Modifier
			.fillMaxSize()
			.size(64.dp)
			.wrapContentSize(Alignment.Center),
		strokeWidth = 4.dp
	)
}

@Composable
fun ContentEmpty() {
	Text(
		text = "Nothing found!",
		modifier = Modifier
			.fillMaxSize()
			.wrapContentSize(Alignment.Center)
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopActionBar(num: Int, callback: () -> Unit) {
	TopAppBar(
		title = {
			Text(
				text = "$num",
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		},
		navigationIcon = {
			IconButton(onClick = callback) {
				Icon(
					imageVector = Icons.Outlined.Close,
					contentDescription = ""
				)
			}
		}
	)
}
package com.karthek.android.s.gallery.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
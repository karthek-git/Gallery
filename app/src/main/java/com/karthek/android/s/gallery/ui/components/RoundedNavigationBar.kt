package com.karthek.android.s.gallery.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun RoundedNavigationBar(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
	Surface(
		color = MaterialTheme.colorScheme.surfaceContainer,
		modifier = modifier
			.windowInsetsPadding(NavigationBarDefaults.windowInsets)
			.padding(16.dp)
			.clip(CircleShape)
	) {
		Row(
			modifier =
				Modifier
					.fillMaxWidth()
					.selectableGroup(),
			horizontalArrangement = Arrangement.spacedBy(4.dp),
			verticalAlignment = Alignment.CenterVertically,
			content = content
		)
	}
}
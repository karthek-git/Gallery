package com.karthek.android.s.gallery.ui.screens.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterNone
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.karthek.android.s.gallery.R

sealed class Screen(val route: String, val icon: ImageVector, @StringRes val res: Int) {
	object Photos : Screen("photos", Icons.Outlined.Image, R.string.title_photos)
	object Explore : Screen("explore", Icons.Outlined.Search, R.string.explore)
	object Albums : Screen("albums", Icons.Outlined.FilterNone, R.string.albums)

	companion object {
		fun fromRoute(route: String?): Screen =
			when (route?.substringBefore("/")) {
				Photos.route -> Photos
				Explore.route -> Explore
				Albums.route -> Albums
				else -> Photos
			}
	}
}

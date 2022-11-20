package com.karthek.android.s.gallery.c.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.karthek.android.s.gallery.c.state.ImageInfoViewModel
import com.karthek.android.s.gallery.c.ui.components.SMediaInfoComponent
import com.karthek.android.s.gallery.state.db.SMedia

@Composable
fun ExternalMediaViewerScreen(sMedia: SMedia, onFinish: () -> Unit) {
	val navController = rememberNavController()
	val onBackClick = { navController.navigateUp(); Unit }
	val onMoreClick = { _: SMedia ->
		navController.navigate("media_view_info")
	}

	NavHost(navController = navController, startDestination = "media_view") {
		composable(route = "media_view") {
			SMediaViewPager(SMediaList = listOf(sMedia),
				initialPage = 0,
				onBackClick = onFinish,
				onMoreClick = onMoreClick)
		}
		composable(
			"media_view_info",
		) {
			val imageInfoViewModel = hiltViewModel<ImageInfoViewModel>()
			LaunchedEffect(
				key1 = sMedia,
				block = { imageInfoViewModel.setImage(sMedia.path, false) }
			)
			SMediaInfoComponent(
				viewModel = imageInfoViewModel,
				onBackClick = onBackClick
			)
		}
	}


}
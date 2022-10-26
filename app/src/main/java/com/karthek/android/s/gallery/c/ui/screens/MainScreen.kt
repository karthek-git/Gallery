package com.karthek.android.s.gallery.c.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.SettingsActivity
import com.karthek.android.s.gallery.c.state.ImageInfoViewModel
import com.karthek.android.s.gallery.c.state.SMViewModel
import com.karthek.android.s.gallery.c.ui.components.MediaInfoView
import com.karthek.android.s.gallery.c.ui.screens.navigation.Screen
import com.karthek.android.s.gallery.state.db.SMedia


val items = listOf(Screen.Photos, Screen.Explore, Screen.Albums)

@Composable
fun MainScreen() {
	Surface {
		Perms {
			MainScreenContent(viewModel = viewModel())
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(viewModel: SMViewModel) {
	val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
	val navController = rememberNavController()
	val navBackStackEntry by navController.currentBackStackEntryAsState()
	val currentDestination = navBackStackEntry?.destination
	val viewingMedia = (currentDestination?.route?.startsWith("media_view") != false)
	val context = LocalContext.current
	Scaffold(topBar = {
		if (!viewingMedia) {
			TopAppBar(title = {
				Text(text = stringResource(Screen.fromRoute(currentDestination?.route).res))
			}, actions = {
				IconButton(onClick = {
					context.startActivity(Intent(context, SettingsActivity::class.java))
				}) {
					Icon(
						imageVector = Icons.Outlined.MoreVert,
						contentDescription = stringResource(R.string.more)
					)
				}
			}, scrollBehavior = scrollBehavior)
		}
	}, bottomBar = {
		if (!viewingMedia) {
			NavigationBar {
				items.forEach { screen ->
					NavigationBarItem(
						selected = currentDestination?.hierarchy?.any {
							it.route != null && it.route!!.startsWith(screen.route)
						} == true,
						onClick = {
							navController.navigate(screen.route) {
								popUpTo(navController.graph.findStartDestination().id) {
									saveState = true
								}
								launchSingleTop = true
								restoreState = true
							}
						},
						icon = { Icon(imageVector = screen.icon, contentDescription = "") },
						label = { Text(stringResource(screen.res)) },
					)
				}
			}
		}
	}, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
		NavContent(navController = navController, viewModel = viewModel, it)
	}
}

@Composable
fun NavContent(
	navController: NavHostController, viewModel: SMViewModel, paddingValues: PaddingValues
) {
	val onBackClick = { navController.navigateUp(); Unit }
	val onMoreClick = { SMedia: SMedia ->
		viewModel.currentSMedia = SMedia
		navController.navigate("media_view_info")
	}
	NavHost(navController = navController, startDestination = "photos") {
		composable("photos") {
			PhotosScreen(viewModel = viewModel, paddingValues = paddingValues,
				callback = { i -> navController.navigate("media_view/-1/$i") })
		}
		composable("explore") {
			ExploreScreen(
				viewModel = viewModel,
				paddingValues = paddingValues,
				onItemClick = { i -> navController.navigate("media_view_explore/-1/$i") }
			)
		}
		composable("albums") {
			FoldersScreen(viewModel = viewModel, paddingValues = paddingValues,
				callback = { navController.navigate("albums/$it") })
		}
		composable(
			"albums/{Index}", arguments = listOf(navArgument("Index") { type = NavType.IntType })
		) {
			val fi = it.arguments?.getInt("Index") ?: -1
			PhotosScreen(viewModel = viewModel, index = fi, paddingValues = paddingValues,
				callback = { i -> navController.navigate("media_view/$fi/$i") })
		}
		composable(
			"media_view/{fi}/{i}", arguments = listOf(navArgument("fi") { type = NavType.IntType },
				navArgument("i") { type = NavType.IntType })
		) { navBackStackEntry ->
			val fi = navBackStackEntry.arguments?.getInt("fi") ?: -1
			val i = navBackStackEntry.arguments?.getInt("i") ?: 0
			val list =
				if (fi == -1) viewModel.sMediaList else viewModel.getFolderContents(fi).value!!.get()
			SMediaViewPager(
				SMediaList = list!!,
				initialPage = i,
				onBackClick = onBackClick,
				onMoreClick = onMoreClick
			)
		}
		composable(
			"media_view_explore/-1/{i}",
			arguments = listOf(navArgument("i") { type = NavType.IntType })
		) { navBackStackEntry ->
			val i = navBackStackEntry.arguments?.getInt("i") ?: 0
			SMediaViewPager(
				SMediaList = viewModel.searchResultSMedia!!,
				initialPage = i,
				onBackClick = onBackClick,
				onMoreClick = onMoreClick
			)
		}
		composable(
			"media_view_info",
		) {
			val imageInfoViewModel = hiltViewModel<ImageInfoViewModel>()
			LaunchedEffect(
				key1 = viewModel.currentSMedia,
				block = { imageInfoViewModel.setImage(viewModel.currentSMedia!!.path) }
			)
			MediaInfoView(
				viewModel = imageInfoViewModel,
				onBackClick = onBackClick
			)
		}
	}
}
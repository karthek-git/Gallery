package com.karthek.android.s.gallery.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.SettingsActivity
import com.karthek.android.s.gallery.state.CategoriesViewModel
import com.karthek.android.s.gallery.state.FacesViewModel
import com.karthek.android.s.gallery.state.ImageInfoViewModel
import com.karthek.android.s.gallery.state.SMViewModel
import com.karthek.android.s.gallery.state.db.SMedia
import com.karthek.android.s.gallery.ui.components.SMediaInfoComponent
import com.karthek.android.s.gallery.ui.screens.navigation.Screen


val items = listOf(Screen.Photos, Screen.Explore, Screen.Albums)

@Composable
fun MainScreen() {
	Surface {
		Perms {
			MainScreenContent(viewModel = viewModel())
		}
	}
}

@Composable
fun MainScreenContent(viewModel: SMViewModel) {
	val rootNavController = rememberNavController()
	val onBackClick = { rootNavController.navigateUp(); Unit }
	val onMoreClick = { SMedia: SMedia ->
		viewModel.currentSMedia = SMedia
		rootNavController.navigate("media_view_info")
	}
	val onFaceItemClick = { index: Int, facesViewModel: FacesViewModel ->
		viewModel.currentSMediaList.list =
			facesViewModel.sFacesWithSMedia!![index].SMediaList.toMutableStateList()
		rootNavController.navigate("dest_view/People")
	}
	val onThingItemClick = { index: Int, categoriesViewModel: CategoriesViewModel ->
		val sCategoryWithSMedia = categoriesViewModel.sCategoriesWithSMedia!![index]
		viewModel.currentSMediaList.list = sCategoryWithSMedia.SMediaList.toMutableStateList()
		rootNavController.navigate("dest_view/${sCategoryWithSMedia.sCategory.name}")
	}
	NavHost(navController = rootNavController, startDestination = "root_host") {
		composable(route = "root_host") {
			RootView(viewModel = viewModel,
				rootNavController = rootNavController,
				onFavouritesClick = {},
				onTrashClick = {},
				onFaceItemClick = onFaceItemClick,
				onThingItemClick = onThingItemClick)
		}
		composable(
			route = "dest_view/{title}",
			arguments = listOf(navArgument("title") {
				type = NavType.StringType
			})
		) { navBackStackEntry ->
			val title = navBackStackEntry.arguments?.getString("title") ?: ""
			DestScreen(title = title,
				viewModel = viewModel,
				onBackClick = onBackClick,
				onItemClick = { i -> rootNavController.navigate("media_view/-1/$i") })
		}
		composable(
			route = "media_view/-1/{i}",
			arguments = listOf(navArgument("i") { type = NavType.IntType })
		) { navBackStackEntry ->
			val i = navBackStackEntry.arguments?.getInt("i") ?: 0
			SMediaViewPager(sMediaList = viewModel.currentSMediaList.list,
				initialPage = i,
				onBackClick = onBackClick,
				onMoreClick = onMoreClick)
		}
		composable(route = "media_view_info") {
			val imageInfoViewModel = hiltViewModel<ImageInfoViewModel>()
			LaunchedEffect(key1 = viewModel.currentSMedia, block = {
				viewModel.currentSMedia?.let { sMedia ->
					imageInfoViewModel.setImage(sMedia.path, sMedia.isVideo)
				}
			})
			SMediaInfoComponent(viewModel = imageInfoViewModel, onBackClick = onBackClick)
		}
		composable(route = "faces_screen") {
			val facesViewModel =
				hiltViewModel<FacesViewModel>(rootNavController.getViewModelStoreOwner(
					rootNavController.graph.id))
			FacesScreen(viewModel = facesViewModel,
				onItemClick = { index -> onFaceItemClick(index, facesViewModel) },
				onBackClick = onBackClick)
		}
		composable(route = "categories_screen") {
			val categoriesViewModel =
				hiltViewModel<CategoriesViewModel>(rootNavController.getViewModelStoreOwner(
					rootNavController.graph.id))
			CategoriesScreen(viewModel = categoriesViewModel,
				onItemClick = { index -> onThingItemClick(index, categoriesViewModel) },
				onBackClick = onBackClick)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootView(
	viewModel: SMViewModel,
	rootNavController: NavHostController,
	onFavouritesClick: () -> Unit,
	onTrashClick: () -> Unit,
	onFaceItemClick: (Int, FacesViewModel) -> Unit,
	onThingItemClick: (Int, CategoriesViewModel) -> Unit,
) {
	val navController = rememberNavController()
	val navBackStackEntry by navController.currentBackStackEntryAsState()
	val currentDestination = navBackStackEntry?.destination
	val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
	val context = LocalContext.current
	Scaffold(topBar = {
		TopAppBar(title = {
			Text(text = stringResource(Screen.fromRoute(currentDestination?.route).res))
		}, actions = {
			IconButton(onClick = {
				context.startActivity(Intent(context, SettingsActivity::class.java))
			}) {
				Icon(imageVector = Icons.Outlined.MoreVert,
					contentDescription = stringResource(R.string.more))
			}
		}, scrollBehavior = scrollBehavior)
	}, bottomBar = {
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
	}, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) { paddingValues ->
		NavContent(rootNavController,
			navController,
			viewModel,
			paddingValues,
			onFavouritesClick,
			onTrashClick,
			onFaceItemClick,
			onThingItemClick)
	}
}

@Composable
fun NavContent(
	rootNavController: NavHostController,
	navController: NavHostController,
	viewModel: SMViewModel,
	paddingValues: PaddingValues,
	onFavouritesClick: () -> Unit,
	onTrashClick: () -> Unit,
	onFaceItemClick: (Int, FacesViewModel) -> Unit,
	onThingItemClick: (Int, CategoriesViewModel) -> Unit,
) {
	NavHost(navController = navController, startDestination = "photos") {
		composable("photos") {
			PhotosScreen(viewModel = viewModel, paddingValues = paddingValues) { i ->
				viewModel.currentSMediaList = viewModel.sMediaList
				rootNavController.navigate("media_view/-1/$i")
			}
		}
		composable("explore") {
			val facesViewModel =
				hiltViewModel<FacesViewModel>(rootNavController.getViewModelStoreOwner(
					rootNavController.graph.id))
			val categoriesViewModel =
				hiltViewModel<CategoriesViewModel>(rootNavController.getViewModelStoreOwner(
					rootNavController.graph.id))
			ExploreScreen(viewModel = viewModel,
				paddingValues = paddingValues,
				onSearchAction = { query -> rootNavController.navigate("dest_view/$query") },
				facesViewModel = facesViewModel,
				onFavouritesClick = onFavouritesClick,
				onTrashClick = onTrashClick,
				onPeopleClick = { rootNavController.navigate("faces_screen") },
				onFaceItemClick = { index -> onFaceItemClick(index, facesViewModel) },
				categoriesViewModel = categoriesViewModel,
				onThingsClick = { rootNavController.navigate("categories_screen") },
				onThingItemClick = { index -> onThingItemClick(index, categoriesViewModel) })
		}
		composable("albums") {
			FoldersScreen(viewModel = viewModel, paddingValues = paddingValues, callback = { i ->
				viewModel.getFolderContents(i)
				rootNavController.navigate("dest_view/${viewModel.folderList?.get(i)?.name}")
			})
		}
	}
}
package com.karthek.android.s.gallery.c.ui.screens

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.k1rakishou.cssi_lib.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.helper.*
import com.karthek.android.s.gallery.state.db.SMedia
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SMediaViewPager(
	SMediaList: List<SMedia>,
	initialPage: Int,
	onBackClick: () -> Unit,
	onMoreClick: (SMedia) -> Unit,
) {
	val view = LocalView.current
	val context = LocalContext.current
	val window = if (context is Activity) context.window else null
	val systemUiController = rememberSystemUiController(window)
	var inImmersiveMode by rememberSaveable { mutableStateOf(false) }
	val pagerState = rememberPagerState(initialPage = initialPage)
	val coroutineScope = rememberCoroutineScope()
	//todo workup slideshow
	var inSlideShow by rememberSaveable { mutableStateOf(false) }
	val onSlideShowClick = {
		coroutineScope.launch {
			inSlideShow = true
			inImmersiveMode = true
			toggleSystemBars(window, view, true, systemUiController)
			slideShowHandler(SMediaList.size, pagerState)
			inImmersiveMode = false
			inSlideShow = false
		}
		Unit
	}
	Surface(color = Color.Black) {
		Box(modifier = Modifier.fillMaxSize()) {
			HorizontalPager(count = SMediaList.size, state = pagerState) {
				SMediaView(SMediaList[it]) {
					if (inSlideShow) return@SMediaView
					inImmersiveMode = !inImmersiveMode
					toggleSystemBars(window, view, inImmersiveMode, systemUiController)
				}
				BackHandler {
					toggleSystemBars(window, view, false, systemUiController)
					if (inSlideShow) {
						inSlideShow = false
						inImmersiveMode = false
						return@BackHandler
					}
					onBackClick()
				}
			}
			if (!inImmersiveMode) {
				SMediaViewControls(
					sMedia = SMediaList[pagerState.currentPage],
					onBackClick = onBackClick,
					onMoreClick = onMoreClick,
					onSlideShowClick = onSlideShowClick
				)
			}
		}
	}
}

private fun toggleSystemBars(
	window: Window?,
	view: View,
	inImmersiveMode: Boolean,
	systemUiController: SystemUiController,
) {
	window ?: return
	val windowInsetsController = WindowCompat.getInsetsController(window, view)
	windowInsetsController.systemBarsBehavior =
		WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
	if (inImmersiveMode) {
		windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
		systemUiController.setStatusBarColor(Color.Black.copy(alpha = 0.5f))
	} else {
		systemUiController.setStatusBarColor(Color.Transparent)
		windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
	}
}

@Composable
fun SMediaView(sMedia: SMedia, onClick: () -> Unit) {
	if (sMedia.isVideo) {
		sMedia.uri?.let { VideoViewComponent(uri = it) }
	} else {
		//SubSampleImage(sMedia = sMedia)
		//ZoomableImage(sMedia = sMedia)
		val context = LocalContext.current
		ComposeSubsamplingScaleImage(
			state = rememberComposeSubsamplingScaleImageState(
				scrollableContainerDirection = ScrollableContainerDirection.Horizontal
			),
			imageSourceProvider = SMediaImageSourceProvider(context, sMedia),
			onImageTapped = { onClick() },
			modifier = Modifier.fillMaxSize()
		)
	}
}

@Composable
fun VideoViewComponent(uri: Uri) {
	//todo functional refactor
	AndroidView(
		factory = { context ->
			VideoView(context)
		},
		modifier = Modifier.fillMaxSize(),
		update = { videoView ->
			videoView.setVideoURI(uri)
			videoView.start()
			videoView.setOnClickListener {
				if (videoView.isPlaying) videoView.pause() else videoView.start()
			}
		}
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxScope.SMediaViewControls(
	sMedia: SMedia,
	onBackClick: () -> Unit,
	onMoreClick: (SMedia) -> Unit,
	onSlideShowClick: () -> Unit,
) {
	val scrimColor = if (!isSystemInDarkTheme()) {
		MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
	} else {
		Color.Black.copy(alpha = 0.5f)
	}
	CompositionLocalProvider(values = arrayOf(LocalContentColor.provides(Color.White))) {
		TopAppBar(
			title = {},
			navigationIcon = {
				IconButton(onClick = onBackClick) {
					Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = "")
				}
			},
			actions = {
				IconButton(onClick = { onMoreClick(sMedia) }) {
					Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "")
				}
			},
			colors = TopAppBarDefaults.smallTopAppBarColors(
				containerColor = Color.Black,
				navigationIconContentColor = LocalContentColor.current,
				actionIconContentColor = LocalContentColor.current
			),
			modifier = Modifier.align(Alignment.TopCenter)
		)
		SMediaViewToolbar(
			sMedia = sMedia, onSlideShowClick = onSlideShowClick, modifier = Modifier
				.align(Alignment.BottomCenter)
				.background(color = Color.Black)
				.navigationBarsPadding()
		)
	}
}

@Composable
fun SMediaViewToolbar(sMedia: SMedia, modifier: Modifier, onSlideShowClick: () -> Unit) {
	val context = LocalContext.current
	Row(
		horizontalArrangement = Arrangement.SpaceEvenly,
		modifier = modifier
			.fillMaxWidth()
			.horizontalScroll(rememberScrollState())
	) {
		ToolbarItem(imageVector = Icons.Outlined.Share,
			title = stringResource(R.string.share),
			onClick = { shareHandler(context, sMedia) })
		ToolbarItem(imageVector = Icons.Outlined.Edit,
			title = stringResource(R.string.edit),
			onClick = { editInHandler(context, sMedia) })
		ToolbarItem(imageVector = Icons.Outlined.Delete,
			title = stringResource(id = R.string.delete),
			onClick = {})
		ToolbarItem(imageVector = Icons.Outlined.ExitToApp,
			title = stringResource(id = R.string.use_as),
			onClick = { useAsHandler(context, sMedia) })
		if (!(sMedia.isVideo)) {
			ToolbarItem(imageVector = Icons.Outlined.Print,
				title = stringResource(id = R.string.print),
				onClick = { printHandler(context, sMedia) })
		}
		ToolbarItem(imageVector = Icons.Outlined.Slideshow,
			title = stringResource(id = R.string.slideshow),
			onClick = onSlideShowClick)
	}
}

@Composable
fun ToolbarItem(imageVector: ImageVector, title: String, onClick: () -> Unit) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.clickable(onClick = onClick)
			.padding(vertical = 14.dp, horizontal = 32.dp)
	) {
		Icon(
			imageVector = imageVector,
			contentDescription = title,
			modifier = Modifier.padding(bottom = 4.dp)
		)
		Text(text = title, style = MaterialTheme.typography.labelLarge)
	}
}

@OptIn(ExperimentalPagerApi::class)
suspend fun slideShowHandler(count: Int, pagerState: PagerState) {
	val initPage = (pagerState.currentPage) + 1
	repeat(count) { i ->
		delay(timeMillis = 5000)
		pagerState.animateScrollToPage(initPage + i)
	}
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SubSampleImage(sMedia: SMedia) {
	val s = SubsamplingScaleImageView(LocalContext.current).apply {
		setImage(ImageSource.uri(sMedia.uri?.toString() ?: "file://${sMedia.path}"))
	}
	AndroidView(factory = {
		s
	}, modifier = Modifier
		.pointerInteropFilter {
			val r = s.onTouchEvent(it)
			Log.v("vittt", "calld $r")
			r
		}
		.fillMaxSize())
}

@Composable
fun ZoomableImage(sMedia: SMedia) {
	var scale by remember { mutableStateOf(1f) }
	var rotation by remember { mutableStateOf(0f) }
	var offset by remember { mutableStateOf(Offset.Zero) }
	val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
		scale = (scale * zoomChange).coerceAtLeast(1f)
		rotation += rotationChange
		offset += offsetChange
	}
	AsyncImage(
		model = ImageRequest.Builder(LocalContext.current).data(sMedia.uri ?: sMedia.path).build(),
		contentDescription = "",
		modifier = Modifier
			.graphicsLayer(
				scaleX = scale,
				scaleY = scale,
				translationX = offset.x,
				translationY = offset.y,
				rotationZ = rotation,
				clip = true
			)
			.transformable(state = state, lockRotationOnZoomPan = true)
			.fillMaxSize()
	)
}
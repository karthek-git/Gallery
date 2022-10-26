package com.karthek.android.s.gallery.c.ui.screens

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.helper.editInHandler
import com.karthek.android.s.gallery.helper.printHandler
import com.karthek.android.s.gallery.helper.shareHandler
import com.karthek.android.s.gallery.helper.useAsHandler
import com.karthek.android.s.gallery.state.db.SMedia

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SMediaViewPager(
	SMediaList: List<SMedia>,
	initialPage: Int,
	onBackClick: () -> Unit,
	onMoreClick: (SMedia) -> Unit
) {
	val pagerState = rememberPagerState(initialPage = initialPage)
	Box {
		HorizontalPager(count = SMediaList.size, state = pagerState) {
			SMediaView(SMediaList[it])
		}
		SMediaViewControls(
			sMedia = SMediaList[pagerState.currentPage],
			onBackClick = onBackClick,
			onMoreClick = onMoreClick
		)
	}
}

@Composable
fun SMediaView(sMedia: SMedia) {
	//SubSampleImage(sMedia = sMedia)
	ZoomableImage(sMedia = sMedia)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxScope.SMediaViewControls(
	sMedia: SMedia,
	onBackClick: () -> Unit,
	onMoreClick: (SMedia) -> Unit
) {
	val scrimColor = if (!isSystemInDarkTheme()) {
		MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
	} else {
		Color.Black.copy(alpha = 0.5f)
	}
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
		colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = scrimColor),
		modifier = Modifier.align(Alignment.TopCenter)
	)
	SMediaViewToolbar(
		sMedia = sMedia, modifier = Modifier
			.align(Alignment.BottomCenter)
			.background(color = scrimColor)
			.navigationBarsPadding()
	)
}

@Composable
fun SMediaViewToolbar(sMedia: SMedia, modifier: Modifier) {
	val context = LocalContext.current
	Row(
		horizontalArrangement = Arrangement.SpaceEvenly,
		modifier = modifier.horizontalScroll(rememberScrollState())
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
		ToolbarItem(imageVector = Icons.Outlined.Print,
			title = stringResource(id = R.string.print),
			onClick = { printHandler(context, sMedia) })
		ToolbarItem(imageVector = Icons.Outlined.Slideshow,
			title = stringResource(id = R.string.slideshow),
			onClick = {})
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
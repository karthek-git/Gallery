package com.karthek.android.s.gallery.ui.screens

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toIntRect
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.state.SMViewModel
import com.karthek.android.s.gallery.state.SMediaStateList
import com.karthek.android.s.gallery.state.db.SMedia
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun PhotosScreen(
	viewModel: SMViewModel,
	paddingValues: PaddingValues,
	callback: (i: Int) -> Unit,
) {
	PhotosScreenContent(sMediaStateList = viewModel.sMediaList, paddingValues, callback)
}

@Composable
fun PhotosScreenContent(
	sMediaStateList: SMediaStateList,
	paddingValues: PaddingValues = PaddingValues(),
	callback: (i: Int) -> Unit,
) {
	if (sMediaStateList.isLoading.value) {
		ContentLoading()
	} else if (sMediaStateList.list.isEmpty()) {
		ContentEmpty()
	} else {
		SMediaGrid(SMediaList = sMediaStateList.list, paddingValues, callback)
	}
}


fun LazyGridState.gridItemKeyAtPosition(hitPoint: Offset): Int? =
	layoutInfo.visibleItemsInfo.find { itemInfo ->
		itemInfo.size.toIntRect().contains(hitPoint.round() - itemInfo.offset)
	}?.key as? Int

fun Modifier.gridDragHandler(
	lazyGridState: LazyGridState,
	selectedIds: MutableState<Set<Int>>,
	autoScrollSpeed: MutableState<Float>,
	autoScrollThreshold: Float,
) = pointerInput(Unit) {
	var initKey: Int? = null
	var currentKey: Int? = null
	detectDragGesturesAfterLongPress(onDragStart = { offset ->
		lazyGridState.gridItemKeyAtPosition(offset)?.let { key ->
			if (!selectedIds.value.contains(key)) {
				initKey = key
				currentKey = key
				selectedIds.value = selectedIds.value + key
			}
		}
	}, onDragCancel = {
		initKey = null
		autoScrollSpeed.value = 0f
	}, onDragEnd = {
		initKey = null
		autoScrollSpeed.value = 0f
	}, onDrag = { change, _ ->
		if (initKey != null) {

			val distFromBottom = lazyGridState.layoutInfo.viewportSize.height - change.position.y
			val distFromTop = change.position.y
			autoScrollSpeed.value = when {
				distFromBottom < autoScrollThreshold -> autoScrollThreshold - distFromBottom
				distFromTop < autoScrollThreshold -> -(autoScrollThreshold - distFromTop)
				else -> 0f
			}

			lazyGridState.gridItemKeyAtPosition(change.position)?.let { key ->
				if (currentKey != key) {
					selectedIds.value = selectedIds.value.minus(initKey!!..currentKey!!)
						.minus(currentKey!!..initKey!!).plus(initKey!!..key).plus(key..initKey!!)
					currentKey = key
				}
			}
		}
	})
}

@Composable
fun SMediaGrid(SMediaList: List<SMedia>, paddingValues: PaddingValues, callback: (i: Int) -> Unit) {
	val selectedIds = rememberSaveable { mutableStateOf(emptySet<Int>()) }
	val inSelectionMode by remember { derivedStateOf { selectedIds.value.isNotEmpty() } }

	val state = rememberLazyGridState()

	val autoScrollSpeed = remember { mutableStateOf(0f) }
	LaunchedEffect(autoScrollSpeed.value) {
		if (autoScrollSpeed.value != 0f) {
			while (isActive) {
				state.scrollBy(autoScrollSpeed.value)
				delay(10)
			}
		}
	}

	LazyVerticalGrid(
		columns = GridCells.Adaptive(112.dp),
		state = state,
		contentPadding = paddingValues,
		verticalArrangement = Arrangement.spacedBy(3.dp),
		horizontalArrangement = Arrangement.spacedBy(3.dp),
		modifier = Modifier.gridDragHandler(lazyGridState = state,
			selectedIds = selectedIds,
			autoScrollSpeed = autoScrollSpeed,
			autoScrollThreshold = with(LocalDensity.current) { 80.dp.toPx() })
	) {
		itemsIndexed(SMediaList, key = { index, _ -> index }) { i, item ->
			val selected = selectedIds.value.contains(i)

			SMediaItem(
				item, selected, inSelectionMode,
				Modifier.clickable {
					if (inSelectionMode) {
						selectedIds.value = if (selected) {
							selectedIds.value.minus(i)
						} else {
							selectedIds.value.plus(i)
						}
					} else {
						callback(i)
					}
				},
			)
		}
	}
}


@Composable
fun SMediaItem(
	sMedia: SMedia,
	selected: Boolean,
	inSelectionMode: Boolean,
	modifier: Modifier,
) {
	Surface(tonalElevation = 3.dp, modifier = modifier.aspectRatio(1f)) {
		Box {
			val transition = updateTransition(selected, label = "selected")
			val imagePadding by transition.animateDp(label = "padding") { selected ->
				if (selected) 10.dp else 0.dp
			}
			val roundedCornerSize by transition.animateDp(label = "corner") { selected ->
				if (selected) 16.dp else 0.dp
			}

			AsyncImage(
				model = ImageRequest.Builder(LocalContext.current).data(sMedia).build(),
				contentDescription = "",
				modifier = Modifier
					.matchParentSize()
					.padding(imagePadding)
					.clip(RoundedCornerShape(roundedCornerSize)),
				contentScale = ContentScale.Crop
			)
			if (sMedia.isVideo) {
				Icon(
					imageVector = Icons.Default.PlayArrow,
					contentDescription = stringResource(R.string.video),
					tint = Color.White,
					modifier = Modifier
						.align(Alignment.TopEnd)
						.size(24.dp)
						.padding(2.dp)
				)
			}
			if (inSelectionMode) {
				if (selected) {
					val bgColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
					Icon(
						Icons.Filled.CheckCircle,
						tint = MaterialTheme.colorScheme.primary,
						contentDescription = null,
						modifier = Modifier
							.padding(4.dp)
							.border(2.dp, bgColor, CircleShape)
							.clip(CircleShape)
							.background(bgColor)
					)
				} else {
					Icon(
						Icons.Filled.RadioButtonUnchecked,
						tint = Color.White.copy(alpha = 0.7f),
						contentDescription = null,
						modifier = Modifier.padding(6.dp)
					)
				}
			}
		}
	}
}
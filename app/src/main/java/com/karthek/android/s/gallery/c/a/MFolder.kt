package com.karthek.android.s.gallery.c.a

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.karthek.android.s.gallery.state.db.SMedia
import java.lang.ref.SoftReference

data class MFolder(
	val path: String,
	val name: String,
	var numItems: Int, //todo goes to state
	val previewSMedia: SMedia,
	val l: MutableState<SoftReference<SnapshotStateList<SMedia>>?> = mutableStateOf(null),
)
package com.karthek.android.s.gallery.c.a

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.karthek.android.s.gallery.state.db.SMedia
import java.lang.ref.SoftReference

data class MFolder(
	val path: String,
	val name: String,
	var numItems: Int,
	val previewSMedia: SMedia,
	val l: MutableState<SoftReference<List<SMedia>>?> = mutableStateOf(null),
)
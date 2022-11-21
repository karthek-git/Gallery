package com.karthek.android.s.gallery.helper

import android.content.Context
import com.github.k1rakishou.cssi_lib.ComposeSubsamplingScaleImageSource
import com.github.k1rakishou.cssi_lib.ImageSourceProvider
import com.karthek.android.s.gallery.state.db.SMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream

class SMediaImageSourceProvider(private val context: Context, private val sMedia: SMedia) :
	ImageSourceProvider {
	override suspend fun provide(): Result<ComposeSubsamplingScaleImageSource> {
		return withContext(Dispatchers.IO) {
			Result.success(ComposeSubsamplingScaleImageSource(sMedia.path,
				getInputStream(context, sMedia)))
		}
	}

	private fun getInputStream(context: Context, sMedia: SMedia): FileInputStream {
		return sMedia.uri?.let { uri ->
			context.contentResolver.openInputStream(uri) as FileInputStream
		} ?: FileInputStream(sMedia.path)
	}
}
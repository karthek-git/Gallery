package com.karthek.android.s.gallery.helper

import com.github.k1rakishou.cssi_lib.ComposeSubsamplingScaleImageSource
import com.github.k1rakishou.cssi_lib.ImageSourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream

class SMediaImageSourceProvider(private val path: String) : ImageSourceProvider {
	override suspend fun provide(): Result<ComposeSubsamplingScaleImageSource> {
		return withContext(Dispatchers.IO) {
			Result.success(ComposeSubsamplingScaleImageSource(path, FileInputStream(path)))
		}
	}
}
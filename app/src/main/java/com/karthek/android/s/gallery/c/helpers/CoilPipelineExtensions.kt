package com.karthek.android.s.gallery.c.helpers

import android.content.ContentResolver
import android.graphics.Bitmap
import androidx.annotation.Px
import androidx.core.graphics.drawable.toDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.key.Keyer
import coil.request.Options
import coil.size.pxOrElse
import com.karthek.android.s.gallery.helper.createBitmap
import com.karthek.android.s.gallery.state.db.SMedia
import java.io.IOException


class SMediaIconFetcher(private val options: Options, private val sMedia: SMedia) : Fetcher {

	override suspend fun fetch(): FetchResult {
		val bitmap = runCatching {
			createImageThumbnail(
				sMedia,
				options.size.width.pxOrElse { 0 },
				options.size.height.pxOrElse { 0 },
				options.context.applicationContext.contentResolver
			)
		}
		val b = bitmap.getOrThrow()
		//todo svg support
		return DrawableResult(b.toDrawable(options.context.resources), true, DataSource.DISK)
	}

	class Factory : Fetcher.Factory<SMedia> {
		override fun create(
			data: SMedia,
			options: Options,
			imageLoader: ImageLoader
		): Fetcher {
			return SMediaIconFetcher(options, data)
		}
	}
}

class SMediaIconKeyer : Keyer<SMedia> {
	override fun key(data: SMedia, options: Options): String {
		return data.uri?.toString() ?: "${data.path}:${data.date}"
	}
}

@Throws(IOException::class)
fun createImageThumbnail(
	sMedia: SMedia,
	@Px reqWidth: Int,
	@Px reqHeight: Int,
	contentResolver: ContentResolver,
): Bitmap {
	return createBitmap(sMedia, reqWidth, reqHeight, contentResolver)
		?: throw IOException("Failed to create thumbnail")
}
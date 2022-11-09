@file:OptIn(DelicateCoroutinesApi::class)

package com.karthek.android.s.gallery.helper

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.annotation.Px
import androidx.exifinterface.media.ExifInterface
import com.karthek.android.s.gallery.state.db.SMedia
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.ensureActive
import java.io.IOException
import kotlin.io.path.Path
import kotlin.io.path.extension

fun createBitmap(
	sMedia: SMedia,
	@Px reqWidth: Int,
	@Px reqHeight: Int,
	contentResolver: ContentResolver,
): Bitmap? {
	// Checkpoint before going deeper
	coroutineContext.ensureActive()
	var bitmap: Bitmap? = null
	if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) && (sMedia.uri != null)) {
		try {
			bitmap =
				contentResolver.loadThumbnail(
					sMedia.uri!!, android.util.Size(reqWidth, reqHeight), null
				)
		} catch (e: IOException) {
			e.printStackTrace()
		}
	} else {
		bitmap = if (sMedia.isVideo) createVideoThumbnail(sMedia.path, reqWidth, reqHeight)
		else createImageThumbnail(sMedia.path, reqWidth, reqHeight)
	}
	return bitmap
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
	// Raw height and width of image
	val height = options.outHeight
	val width = options.outWidth
	var inSampleSize = 1
	if (height > reqHeight || width > reqWidth) {
		val halfHeight = height / 2
		val halfWidth = width / 2

		// Calculate the largest inSampleSize value that is a power of 2 and keeps both
		// height and width larger than the requested height and width.
		while (halfHeight / inSampleSize >= reqHeight
			&& halfWidth / inSampleSize >= reqWidth
		) {
			inSampleSize *= 2
		}
	}
	return inSampleSize
}

@Throws(IOException::class)
fun createImageThumbnail(
	path: String,
	@Px reqWidth: Int,
	@Px reqHeight: Int
): Bitmap {
	// Checkpoint before going deeper
	coroutineContext.ensureActive()
	var bitmap: Bitmap? = null
	val exif = ExifInterface(path)
	var orientation = 0
	val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(Path(path).extension)

	// get orientation
	when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)) {
		ExifInterface.ORIENTATION_ROTATE_90 -> orientation = 90
		ExifInterface.ORIENTATION_ROTATE_180 -> orientation = 180
		ExifInterface.ORIENTATION_ROTATE_270 -> orientation = 270
	}
	if (mimeType == "image/heif" || mimeType == "image/heif-sequence" || mimeType == "image/heic" || mimeType == "image/heic-sequence") {
		try {
			MediaMetadataRetriever().use { retriever ->
				retriever.setDataSource(path)
				/* TODO heif */
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
					bitmap = retriever.getImageAtIndex(
						-1,
						MediaMetadataRetriever.BitmapParams()
					)
				}
			}
		} catch (e: RuntimeException) {
			throw IOException("Failed to create thumbnail", e)
		}
	}
	if (bitmap == null) {
		val raw = exif.thumbnailBytes
		if (raw != null) {
			try {
				bitmap = decodeBitmap(raw, reqWidth, reqHeight)
			} catch (e: IOException) {
				throw IOException("Failed to decode Bitmap", e)
			}
		}
	}

	// Checkpoint before going deeper
	coroutineContext.ensureActive()
	if (bitmap == null) {
		bitmap = decodeBitmap(path, reqWidth, reqHeight)
		// Use ImageDecoder to do full file decoding, we don't need to handle the orientation
		return bitmap ?: throw  IOException("Failed to decode file")
	}

	// Transform the bitmap if the orientation of the image is not 0.
	if (orientation != 0) {
		val width = bitmap!!.width
		val height = bitmap!!.height
		val m = Matrix()
		m.setRotate(orientation.toFloat(), (width / 2).toFloat(), (height / 2).toFloat())
		bitmap = Bitmap.createBitmap(bitmap!!, 0, 0, width, height, m, false)
	}
	return bitmap ?: throw IOException("Failed to create thumbnail")
}

@Throws(IOException::class)
fun createVideoThumbnail(path: String, @Px reqWidth: Int, @Px reqHeight: Int): Bitmap {
	// Checkpoint before going deeper
	coroutineContext.ensureActive()
	try {
		MediaMetadataRetriever().use { mmr ->
			mmr.setDataSource(path)

			// Try to retrieve thumbnail from metadata
			val raw = mmr.embeddedPicture
			if (raw != null) {
				return decodeBitmap(raw, reqWidth, reqHeight)
			}
			val width =
				mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!.toInt()
			val height =
				mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!.toInt()
			// Fall back to middle of video
			// Note: METADATA_KEY_DURATION unit is in ms, not us.
			val thumbnailTimeUs =
				mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
					.toLong() * 1000 / 2

			// If we're okay with something larger than native format, just
			// return a frame without up-scaling it
			return if (reqWidth < width && reqHeight < height && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
				mmr.getScaledFrameAtTime(
					thumbnailTimeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
					reqWidth, reqHeight
				)!!
			} else {
				mmr.getFrameAtTime(
					thumbnailTimeUs,
					MediaMetadataRetriever.OPTION_CLOSEST_SYNC
				)!!
			}
		}
	} catch (e: RuntimeException) {
		throw IOException("Failed to create thumbnail", e)
	}
}

private fun decodeBitmap(path: String, reqWidth: Int, reqHeight: Int): Bitmap {
	return BitmapFactory.Options().run {
		inJustDecodeBounds = true
		BitmapFactory.decodeFile(path, this)
		inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
		inJustDecodeBounds = false
		BitmapFactory.decodeFile(path, this)
	}
}

private fun decodeBitmap(raw: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap {
	return BitmapFactory.Options().run {
		inJustDecodeBounds = true
		BitmapFactory.decodeByteArray(raw, 0, raw.size, this)
		inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
		inJustDecodeBounds = false
		BitmapFactory.decodeByteArray(raw, 0, raw.size, this)
	}
}

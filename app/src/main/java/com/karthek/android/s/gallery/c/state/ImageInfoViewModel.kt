package com.karthek.android.s.gallery.c.state

import android.app.Application
import android.graphics.BitmapFactory
import android.media.MediaCodecInfo
import android.media.MediaMetadataRetriever
import android.text.format.Formatter
import androidx.annotation.Px
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class ImageInfoViewModel @Inject constructor(private var application: Application) : ViewModel() {

	var album by mutableStateOf("")

	var path by mutableStateOf("")

	var name by mutableStateOf("")

	var takenDate by mutableStateOf("")

	var modifiedDate by mutableStateOf("")

	var size by mutableStateOf("")

	var oem by mutableStateOf("")

	var params by mutableStateOf("")

	var hasExifMetaData by mutableStateOf(false)

	var isVideo by mutableStateOf(false)

	var mimetype by mutableStateOf("")

	var videoCodec by mutableStateOf("")

	var audioCodec by mutableStateOf("")

	fun setImage(path: String, isVideo: Boolean) {
		this.path = path
		this.isVideo = isVideo
		viewModelScope.launch(Dispatchers.Default) { loadInfo() }
	}

	private fun loadInfo() {
		//todo better handle uri
		val file = File(path)
		album = file.parentFile?.name ?: ""
		name = file.name
		val simpleDateFormat = SimpleDateFormat("E MMM d, y h:m a", Locale.getDefault())
		simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
		modifiedDate = "Modified:  ${simpleDateFormat.format(Date(file.lastModified()))}"
		if (isVideo) loadVideoInfo(file) else loadImageInfo(file, simpleDateFormat)
	}

	private fun loadImageInfo(file: File, simpleDateFormat: SimpleDateFormat) {
		try {
			val bitmapOpts = BitmapFactory.Options()
			bitmapOpts.inJustDecodeBounds = true
			BitmapFactory.decodeFile(path, bitmapOpts)
			mimetype = bitmapOpts.outMimeType
			if (ExifInterface.isSupportedMimeType(mimetype)) {
				val exifInterface = ExifInterface(file)
				hasExifMetaData = exifInterface.hasAttribute(ExifInterface.TAG_EXIF_VERSION)
				if (hasExifMetaData) {
					hasExifMetaData = true
					takenDate =
						"Taken: ${simpleDateFormat.format(Date(exifInterface.dateTimeOriginal!!))}"
					oem =
						exifInterface.getAttribute(ExifInterface.TAG_MAKE) + " " + exifInterface.getAttribute(
							ExifInterface.TAG_MODEL)
					val length = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
					val width = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
					setSizeField(width, length, file.length())
					val aperture =
						exifInterface.getAttributeDouble(ExifInterface.TAG_APERTURE_VALUE, 0.0)
					val exposureTime =
						(0.5 + 1 / exifInterface.getAttributeDouble(ExifInterface.TAG_EXPOSURE_TIME,
							0.0)).toInt()
					val focalLength =
						exifInterface.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0.0)
					val iso = exifInterface.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)
					params = String.format(Locale.ENGLISH,
						"f/%.1f   1/%d   %.2fmm   ISO%s",
						aperture,
						exposureTime,
						focalLength,
						iso)
				}
			}
			if (!hasExifMetaData) {
				setSizeField(bitmapOpts.outWidth, bitmapOpts.outHeight, file.length())
			}
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	private fun loadVideoInfo(file: File) {
		val width: Int
		val height: Int
		MediaMetadataRetriever().use { retriever ->
			retriever.setDataSource(file.path)
			mimetype = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: ""
			width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
				?.toIntOrNull() ?: 0
			height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
				?.toIntOrNull() ?: 0
		}
		setSizeField(width, height, file.length())
	}

	private fun setSizeField(@Px width: Int, @Px height: Int, fileSize: Long) {
		val mp = ((width * height) / 1000000f).roundToInt()
		size = String.format(Locale.ENGLISH,
			"%dMP   %sx%s   %s",
			mp,
			height,
			width,
			Formatter.formatFileSize(application, fileSize))
	}
}
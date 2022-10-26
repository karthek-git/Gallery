package com.karthek.android.s.gallery.c.state

import android.app.Application
import android.text.format.Formatter
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

	var visibility by mutableStateOf(false)

	fun setImage(path: String) {
		this.path = path
		viewModelScope.launch(Dispatchers.Default) { loadInfo() }
	}

	private fun loadInfo() {
		val file = File(this.path)
		album = file.parentFile?.name ?: ""
		name = file.name
		val simpleDateFormat = SimpleDateFormat("E MMM d, y h:m a", Locale.getDefault())
		simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
		modifiedDate = "Modified:  ${simpleDateFormat.format(Date(file.lastModified()))}"
		try {
			val exifInterface = ExifInterface(file)
			if (!exifInterface.hasAttribute(ExifInterface.TAG_EXIF_VERSION)) {
				visibility = false
				return
			} else {
				visibility = true
			}
			takenDate = "Taken: ${simpleDateFormat.format(Date(exifInterface.dateTimeOriginal!!))}"
			oem =
				exifInterface.getAttribute(ExifInterface.TAG_MAKE) + " " +
						exifInterface.getAttribute(ExifInterface.TAG_MODEL)
			val length = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
			val width = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
			val mp = (length * width / 1000000f).roundToInt()
			size = String.format(
				Locale.ENGLISH, "%dMP   %sx%s   %s", mp, length, width,
				Formatter.formatFileSize(application, file.length())
			)
			val aperture = exifInterface.getAttributeDouble(ExifInterface.TAG_APERTURE_VALUE, 0.0)
			val exposureTime = (0.5 + 1 / exifInterface.getAttributeDouble(
				ExifInterface.TAG_EXPOSURE_TIME,
				0.0
			)).toInt()
			val focalLength = exifInterface.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0.0)
			val iso = exifInterface.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)
			params =
				String.format(
					Locale.ENGLISH, "f/%.1f   1/%d   %.2fmm   ISO%s",
					aperture,
					exposureTime, focalLength, iso
				)
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}
}
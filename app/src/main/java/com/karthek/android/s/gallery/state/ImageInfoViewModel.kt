package com.karthek.android.s.gallery.state

import android.app.Application
import android.text.format.Formatter
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class ImageInfoViewModel @Inject constructor(private var application: Application) : ViewModel() {
    @JvmField
    var album = MutableLiveData<String>()

    @JvmField
    var path = MutableLiveData<String>()

    @JvmField
    var name = MutableLiveData<String>()

    @JvmField
    var takenDate = MutableLiveData<String>()

    @JvmField
    var modifiedDate = MutableLiveData<String>()

    @JvmField
    var size = MutableLiveData<String>()

    @JvmField
    var oem = MutableLiveData<String>()

    @JvmField
    var params = MutableLiveData<String>()

    @JvmField
    var visibility = MutableLiveData<Boolean>()

    fun setImage(path: String) {
        this.path.value = path
        viewModelScope.launch(Dispatchers.Default) { loadInfo() }
    }

    private fun loadInfo() {
        val path = Paths.get(path.value)
        album.postValue(path.parent.fileName.toString())
        name.postValue(path.fileName.toString())
        val simpleDateFormat = SimpleDateFormat("E MMM d, y h:m a", Locale.getDefault())
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        modifiedDate.postValue(
            "Modified:  ${simpleDateFormat.format(Date(path.toFile().lastModified()))}"
        )
        try {
            val exifInterface = ExifInterface(path.toString())
            if (!exifInterface.hasAttribute(ExifInterface.TAG_EXIF_VERSION)) {
                visibility.postValue(false)
                return
            } else {
                visibility.postValue(true)
            }
            takenDate.postValue("Taken: ${simpleDateFormat.format(Date(exifInterface.dateTimeOriginal!!))}")
            oem.postValue(
                exifInterface.getAttribute(ExifInterface.TAG_MAKE) + " " +
                        exifInterface.getAttribute(ExifInterface.TAG_MODEL)
            )
            val length = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
            val width = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
            val mp = (length * width / 1000000f).roundToInt()
            size.postValue(
                String.format(
                    Locale.ENGLISH, "%dMP   %sx%s   %s", mp, length, width,
                    Formatter.formatFileSize(application, path.toFile().length())
                )
            )
            val aperture = exifInterface.getAttributeDouble(ExifInterface.TAG_APERTURE_VALUE, 0.0)
            val exposureTime = (0.5 + 1 / exifInterface.getAttributeDouble(
                ExifInterface.TAG_EXPOSURE_TIME,
                0.0
            )).toInt()
            val focalLength = exifInterface.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0.0)
            val iso = exifInterface.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)
            params.postValue(
                String.format(
                    Locale.ENGLISH, "f/%.1f   1/%d   %.2fmm   ISO%s",
                    aperture,
                    exposureTime, focalLength, iso
                )
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
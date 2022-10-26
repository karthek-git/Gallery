package com.karthek.android.s.gallery.helper

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.Px
import com.karthek.android.s.gallery.state.db.SMedia
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.ensureActive
import java.io.IOException

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
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(sMedia.path, options)
        options.inJustDecodeBounds = false
        options.inSampleSize = calculateInSampleSize(
            options,
            reqWidth,
            reqWidth
        )
        bitmap = BitmapFactory.decodeFile(sMedia.path, options)
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

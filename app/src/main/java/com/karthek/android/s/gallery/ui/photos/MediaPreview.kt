package com.karthek.android.s.gallery.ui.photos

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Size
import android.widget.ImageView
import com.karthek.android.s.gallery.R
import com.karthek.android.s.gallery.helper.calculateInSampleSize
import java.io.IOException

class MediaPreview(
	private var viewHolder: PhotosRecyclerViewAdapter.ViewHolder,
	private var position: Int
) : Runnable {
	private val contentResolver = viewHolder.imageView.context.applicationContext.contentResolver
	var size = Size(300, 300)
	override fun run() {
		var bitmap: Bitmap? = null
		if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) && (viewHolder.mItem.uri != null)) {
			try {
				bitmap =
					contentResolver.loadThumbnail(
						viewHolder.mItem.uri!!, size, null
					)
			} catch (e: IOException) {
				e.printStackTrace()
			}
		} else {
			val options = BitmapFactory.Options()
			options.inJustDecodeBounds = true
			BitmapFactory.decodeFile(viewHolder.mItem.path, options)
			options.inJustDecodeBounds = false
			options.inSampleSize = calculateInSampleSize(options, 300, 300)
			bitmap = BitmapFactory.decodeFile(viewHolder.mItem.path, options)
		}
		if (bitmap != null) {
			PhotosFragment.addBitmapToMemoryCache(viewHolder.mItem.path, bitmap)
			if (viewHolder.pos == position) {
				val finalBitmap: Bitmap = bitmap
				viewHolder.imageView.post { viewHolder.imageView.setImageBitmap(finalBitmap) }
			}
		} else {
			if (viewHolder.pos == position) {
				viewHolder.imageView.post {
					viewHolder.imageView.scaleType = ImageView.ScaleType.CENTER
					viewHolder.imageView.setImageResource(R.drawable.ic_image_not_supported)
				}
			}
		}
	}
}
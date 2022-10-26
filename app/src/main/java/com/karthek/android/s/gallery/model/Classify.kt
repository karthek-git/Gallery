package com.karthek.android.s.gallery.model

import android.content.Context
import android.graphics.Bitmap
import com.karthek.android.s.gallery.ml.EfficientnetLite0Int82
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model

class Classify(context: Context) {
	val model: EfficientnetLite0Int82

	init {
		val options = Model.Options.Builder().setDevice(Model.Device.NNAPI).build()
		model = EfficientnetLite0Int82.newInstance(context, options)
	}

	protected fun finalize() {
		model.close()
	}

	fun getCategory(bitmap: Bitmap): String {
		val outputs = model.process(TensorImage.fromBitmap(bitmap))
		val probability =
			outputs.probabilityAsCategoryList.filter { category -> category.score > 0.5f }
		return StringBuilder().apply { probability.forEach { p -> append(p.label) } }.toString()
	}
}
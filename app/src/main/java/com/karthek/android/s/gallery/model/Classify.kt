package com.karthek.android.s.gallery.model

import android.content.Context
import android.graphics.Bitmap
import com.karthek.android.s.gallery.ml.EfficientnetLite2Uint82
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import org.tensorflow.lite.task.vision.classifier.ImageClassifier.ImageClassifierOptions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Classify @Inject constructor(@ApplicationContext context: Context) {
	private val model: EfficientnetLite2Uint82
	private val imageSceneClassifier: ImageClassifier

	init {
		val options = Model.Options.Builder().setDevice(Model.Device.NNAPI).build()
		model = EfficientnetLite2Uint82.newInstance(context, options)
		val sceneImageClassifierOptions = ImageClassifierOptions.builder()
			.setBaseOptions(BaseOptions.builder().build())
			.setMaxResults(1)
			.build()
		imageSceneClassifier = ImageClassifier.createFromFileAndOptions(context,
			"image_scene_uint8_1.tflite",
			sceneImageClassifierOptions)
	}

	protected fun finalize() {
		model.close()
	}

	fun getCategory(bitmap: Bitmap): String {
		val tensorImage = TensorImage.fromBitmap(bitmap)
		val outputs = model.process(tensorImage)
		val probability =
			outputs.probabilityAsCategoryList.filter { category -> category.score > 0.05f }
		return StringBuilder().apply { probability.forEach { p -> append(p.label) } }
			.append(getSceneCategory(tensorImage))
			.toString()
	}

	private fun getSceneCategory(tensorImage: TensorImage): String {
		return imageSceneClassifier.classify(tensorImage)[0].categories
			.maxBy { category -> category.score }
			.label
	}
}

const val CLASSIFY_ML_VERSION = 1
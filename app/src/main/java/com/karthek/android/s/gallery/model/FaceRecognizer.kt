package com.karthek.android.s.gallery.model

import android.content.Context
import android.graphics.Bitmap
import com.karthek.android.s.gallery.ml.FacenetMobileV1
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.model.Model
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaceRecognizer @Inject constructor(
	@ApplicationContext context: Context,
	private val faceDetector: FaceDetector,
) {
	private val model: FacenetMobileV1

	private val imageProcessor = ImageProcessor.Builder()
		.add(ResizeOp(112, 112, ResizeOp.ResizeMethod.BILINEAR))
		.build()

	init {
		val options = Model.Options.Builder().setDevice(Model.Device.NNAPI).build()
		model = FacenetMobileV1.newInstance(context, options)
	}

	protected fun finalize() {
		model.close()
	}

	suspend fun getFaceEmbeddings(bitmap: Bitmap): List<FloatArray> {
		val faceEmbeddings = mutableListOf<FloatArray>()
		val faceBitmaps = faceDetector.getFaceBitmaps(bitmap)
		faceBitmaps.forEach { faceBitmap -> faceEmbeddings.add(getFaceEmbedding(faceBitmap)) }
		return faceEmbeddings
	}

	private fun getFaceEmbedding(faceBitmap: Bitmap): FloatArray {
		val image = imageProcessor.process(TensorImage.fromBitmap(faceBitmap))
		val outputs = model.process(image)
		return outputs.embeddingAsTensorBuffer.floatArray
	}
}

const val N_EMBEDDING_DIMENSIONS = 192
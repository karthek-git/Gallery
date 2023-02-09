package com.karthek.android.s.gallery.ml.model

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

@Singleton
class FaceRecognizer @Inject constructor(
	@ApplicationContext context: Context,
	private val faceDetector: FaceDetector,
) {
	private val model: Model
	private val imageProcessor: ImageProcessor


	init {
		val options = Model.Options.Builder().setDevice(Model.Device.NNAPI).build()
		model = Model.createModel(context, "facenet_mobile_v1.tflite", options)
		imageProcessor = ImageProcessor.Builder()
			.add(ResizeOp(112, 112, ResizeOp.ResizeMethod.BILINEAR))
			.add(TensorOperator { preWhiten(it) })
			.build()
	}

	protected fun finalize() {
		model.close()
	}

	private fun preWhiten(tensorBuffer: TensorBuffer): TensorBuffer {
		val arr = tensorBuffer.floatArray
		val mean = arr.average().toFloat()
		val std = sqrt(arr.fold(0.0f) {
				accumulator, next -> accumulator + (next - mean).pow(2.0f)
		} / arr.size)
		val stdAdj = max(std, 1.0f / sqrt(arr.size.toFloat()))
		return NormalizeOp(mean,stdAdj).apply(tensorBuffer)
	}

	suspend fun getFaceEmbeddings(bitmap: Bitmap): List<FloatArray> {
		val faceEmbeddings = mutableListOf<FloatArray>()
		//val faceBitmaps = faceDetector.getFaceBitmaps(bitmap)
		//faceBitmaps.forEach { faceBitmap -> faceEmbeddings.add(getFaceEmbedding(faceBitmap)) }
		return faceEmbeddings
	}

	private fun getFaceEmbedding(faceBitmap: Bitmap): FloatArray {
		val imageBuffer = imageProcessor.process(TensorImage.fromBitmap(faceBitmap)).buffer
		val output = TensorBuffer
			.createFixedSize(model.getOutputTensorShape(0), DataType.FLOAT32)
		model.run(arrayOf(imageBuffer), mapOf(0 to output.buffer))
		return output.floatArray
	}
}

const val N_EMBEDDING_DIMENSIONS = 192
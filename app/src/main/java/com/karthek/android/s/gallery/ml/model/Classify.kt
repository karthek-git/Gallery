package com.karthek.android.s.gallery.ml.model

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.DequantizeOp
import org.tensorflow.lite.support.common.ops.QuantizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Classify @Inject constructor(@ApplicationContext context: Context) {
	private val model: Model
	private val imageProcessor: ImageProcessor
	private val probabilityPostProcessor: TensorProcessor

	init {
		val options = Model.Options.Builder().setDevice(Model.Device.CPU).build()
		model = Model.createModel(context, "gic_uint8_v1.tflite", options)

		val iQuantizationParams = model.getInputTensor(0).quantizationParams()
		imageProcessor = ImageProcessor.Builder()
			.add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
			.add(QuantizeOp(iQuantizationParams.zeroPoint.toFloat(), iQuantizationParams.scale))
			.add(CastOp(DataType.UINT8))
			.build()

		val oQuantizationParams = model.getOutputTensor(0).quantizationParams()
		probabilityPostProcessor = TensorProcessor.Builder()
			.add(DequantizeOp(oQuantizationParams.zeroPoint.toFloat(),
				oQuantizationParams.scale))
			.build()
	}

	protected fun finalize() {
		model.close()
	}

	fun getCategory(bitmap: Bitmap): List<Int> {
		val imageBuffer = imageProcessor.process(TensorImage.fromBitmap(bitmap)).buffer
		val output = TensorBuffer
			.createFixedSize(model.getOutputTensorShape(0), DataType.UINT8)
		model.run(arrayOf(imageBuffer), mapOf(0 to output.buffer))
		return probabilityPostProcessor.process(output)
			.floatArray
			.withIndex()
			.filter { it.value > 0.5 }
			.map { it.index }
	}
}

const val CLASSIFY_ML_VERSION = 1
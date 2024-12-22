package com.karthek.android.s.gallery.state

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.util.Size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthek.android.s.gallery.ml.model.FaceDetector
import com.karthek.android.s.gallery.ml.model.FaceRecognizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.DequantizeOp
import org.tensorflow.lite.support.common.ops.QuantizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ImageInfScreenViewModel @Inject constructor(private val appContext: Application) :
	ViewModel() {
	var imageUri by mutableStateOf<Uri?>(null)
	var infInProgress by mutableStateOf(false)
	var infOutText by mutableStateOf("")
	var imgBitmap by mutableStateOf<Bitmap?>(null)

	fun onRunClick() {
		viewModelScope.launch {
			infInProgress = true
			infOutText = withContext(Dispatchers.Default) {
				runClassify()
				//faceDetect()
			}
			infInProgress = false
		}
	}

	 fun getBitmap(w: Int, h: Int): Bitmap? {
		var bitmap: Bitmap? = null
		try {
			bitmap = appContext.contentResolver.loadThumbnail(
				imageUri!!,
				Size(w, h), null
			)
		} catch (e: IOException) {
			e.printStackTrace()
		}
		return bitmap
	}

	private fun runClassify(): String {
		var ret = "Nothing"
		val options = Model.Options.Builder().setDevice(Model.Device.NNAPI).build()
		try {
			val model = Model.createModel(appContext, "gic_uint8_v1.tflite", options)

			val iQuantizationParams = model.getInputTensor(0).quantizationParams()

			val imageProcessor = ImageProcessor.Builder()
				.add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
				//.add(NormalizeOp(127.5f, 127.5f))
				.add(QuantizeOp(iQuantizationParams.zeroPoint.toFloat(), iQuantizationParams.scale))
				.add(CastOp(DataType.UINT8))
				.build()

			val oQuantizationParams = model.getOutputTensor(0).quantizationParams()

			val probabilityPostProcessor = TensorProcessor.Builder()
				.add(
					DequantizeOp(
						oQuantizationParams.zeroPoint.toFloat(),
						oQuantizationParams.scale
					)
				)
				.build()

			val labels = FileUtil.loadLabels(appContext, "gic_labels.txt")

			val image = imageProcessor.process(TensorImage.fromBitmap(getBitmap(224, 224)))

			val output = TensorBuffer.createFixedSize(model.getOutputTensorShape(0), DataType.UINT8)
			model.run(arrayOf(image.buffer), mapOf(0 to output.buffer))
			val probability =
				TensorLabel(labels, probabilityPostProcessor.process(output)).categoryList
			probability.filter { category: Category -> category.score != 0f }
			probability.sortWith { o1: Category, o2: Category ->
				o2.score.compareTo(o1.score)
			}
			val s = StringBuilder("Inference:\n\n")
			for (c in probability) {
				s.append(String.format(Locale.ENGLISH, "%-20s %f\n", c.label, c.score))
			}
			ret = s.toString()
			model.close()
		} catch (e: IOException) {
			e.printStackTrace()
		}
		return ret
	}

//	private fun faceDetect(): String {
//		var ret = "Nothing"
//		val faceDetectionOptions = FaceDetectionOptions.builder()
//			.setStaticImageMode(true)
//			.setModelSelection(1)
//			.build()
//		val faceDetection = FaceDetection(appContext, faceDetectionOptions)
//		val bitmap = getBitmap(192, 192)
//		faceDetection.send(bitmap)
//		faceDetection.setResultListener { result: FaceDetectionResult ->
//			val detectionList: List<DetectionProto.Detection> = result.multiFaceDetections()
//			Log.v("faced", "got res " + detectionList.size)
//			if (detectionList.isEmpty()) {
//				return@setResultListener
//			}
//			val width = result.inputBitmap().width
//			val height = result.inputBitmap().height
//			val bitmap1 = bitmap!!.copy(bitmap.config, true)
//			val canvas = Canvas(bitmap1)
//			for (detection in detectionList) {
//				drawBoundingBoxes(detection, canvas, width, height)
//			}
//			val detection = detectionList[0]
//			val left = detection.locationData.relativeBoundingBox.xmin * width
//			val top = detection.locationData.relativeBoundingBox.ymin * height
//			val right = detection.locationData.relativeBoundingBox.width * width
//			val bottom = detection.locationData.relativeBoundingBox.height * height
//			val leftEye = detection.locationData.getRelativeKeypoints(FaceKeypoint.LEFT_EYE)
//			val rightEye = detection.locationData.getRelativeKeypoints(FaceKeypoint.RIGHT_EYE)
//			val lx = leftEye.x * width
//			val ly = leftEye.y * height
//			val rx = rightEye.x * width
//			val ry = rightEye.y * height
//			val dY = ry - ly
//			val dX = rx - lx
//			val eyesCenter = PointF((lx + rx) / 2, (ly + ry) / 2)
//			val angle = ((Math.toDegrees(atan2(dY, dX).toDouble())) - 180).toFloat()
//			val curDist = sqrt((dX * dX) + (dY * dY))
//			val reqDist = (0.30f) * 128
//			val scale = reqDist / curDist
//			val a = scale * cos(angle)
//			val b = scale * sin(angle)
//			val matrix = Matrix()
//			matrix.setValues(
//				floatArrayOf(
//					a,
//					((1 - a) * (eyesCenter.x) - b * (eyesCenter.y)),
//					a + (128 * 0.5f - eyesCenter.x),
//					b,
//					-b,
//					((b * (eyesCenter.x) + (1 - a) * (eyesCenter.y)) + (128 * 0.35f - eyesCenter.y)),
//					0f,
//					0f,
//					1f
//				)
//			)
//			//matrix.setRotate(angle, eyesCenter.x, eyesCenter.y)
//			val croppedBitmap = Bitmap.createBitmap(
//				bitmap,
//				0,
//				0,
//				width,
//				height, matrix, true
//			)
//			//faceRecognize(croppedBitmap)
//			Log.v("faced", String.format("got %f %f %f %f", left, top, right, bottom))
//			imgBitmap = bitmap1
//			ret = String.format(
//				Locale.ENGLISH,
//				"detected faces: %d",
//				detectionList.size
//			)
//		}
//		return ret
//	}

	private suspend fun faceRecognize2() {
		val faceRecognizer = FaceRecognizer(appContext, FaceDetector(appContext))
		getBitmap(192, 192)?.let {
			Log.v("recog2", faceRecognizer.getFaceEmbeddings(it)[0].contentToString())
		}
	}

//	private fun drawBoundingBoxes(
//		detection: DetectionProto.Detection, canvas: Canvas, width: Int,
//		height: Int,
//	) {
//		Log.v("faced", "drwing on canvas")
//		val bboxPaint = Paint()
//		bboxPaint.color = Color.GREEN
//		bboxPaint.style = Paint.Style.STROKE
//		bboxPaint.strokeWidth = 5f
//		val left = detection.locationData.relativeBoundingBox.xmin * width
//		val top = detection.locationData.relativeBoundingBox.ymin * height
//		val right = left + detection.locationData.relativeBoundingBox.width * width
//		val bottom = top + detection.locationData.relativeBoundingBox.height * height
//		val leftEye = detection.locationData.getRelativeKeypoints(FaceKeypoint.LEFT_EYE)
//		val rightEye = detection.locationData.getRelativeKeypoints(FaceKeypoint.RIGHT_EYE)
//		val lx = leftEye.x * width
//		val ly = leftEye.y * height
//		val rx = rightEye.x * width
//		val ry = rightEye.y * height
//		val dY = ry - ly
//		val dX = rx - lx
//		val eyesCenter = PointF((lx + rx) / 2, (ly + ry) / 2)
//		canvas.drawRect(left, top, right, bottom, bboxPaint)
//		canvas.drawPoint(lx, ly, bboxPaint)
//		canvas.drawPoint(rx, ry, bboxPaint)
//		canvas.drawPoint(eyesCenter.x, eyesCenter.y, bboxPaint)
//	}

//	private fun tfLiteTask() {
//		try {
//			val bitmap = getBitmap(224, 224)
//			val image = TensorImage.fromBitmap(bitmap)
//			val options = ImageClassifierOptions.builder()
//				.setBaseOptions(BaseOptions.builder().useGpu()
//					.build())
//				.setMaxResults(5)
//				.build()
//			val imageClassifier = ImageClassifier.createFromFileAndOptions(requireContext(),
//				"gic_uint8_v1.tflite",
//				options)
//
//			val labels = FileUtil.loadLabels(requireContext(), "gic_labels.txt")
//			val results = imageClassifier.classify(image)
//			val probability = results[0].categories
//			val s = StringBuilder("Inference:\n\n")
//			for (c in probability) {
//				s.append(String.format(Locale.ENGLISH, "%-20s %f\n", labels[c.label.toInt()] ,c.score))
//			}
//			binding!!.textDashboard.text = s
//		} catch (e: IOException) {
//			e.printStackTrace()
//		}
//	}
}
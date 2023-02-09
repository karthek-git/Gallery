package com.karthek.android.s.gallery.ml.model


import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaceDetector @Inject constructor(@ApplicationContext context: Context) {
/*	private val faceDetection: FaceDetection

	init {
		val faceDetectionOptions = FaceDetectionOptions.builder()
			.setStaticImageMode(true)
			.setModelSelection(1)
			.build()
		faceDetection = FaceDetection(context, faceDetectionOptions)
	}

	suspend fun getFaceBitmaps(bitmap: Bitmap): List<Bitmap> {
		return suspendCoroutine { continuation ->
			faceDetection.send(bitmap)
			faceDetection.setResultListener { result ->
				continuation.resume(getFaceBitmaps(bitmap, result))
			}
		}
	}

	private fun getFaceBitmaps(bitmap: Bitmap, result: FaceDetectionResult): List<Bitmap> {
		val faceBitmaps = mutableListOf<Bitmap>()
		val detectionList = result.multiFaceDetections()
		if (detectionList.isEmpty()) return faceBitmaps
		detectionList.forEach { detection ->
			try {
				faceBitmaps.add(getFaceBitmap(bitmap, detection))
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
		return faceBitmaps
	}

	private fun getFaceBitmap(bitmap: Bitmap, detection: Detection): Bitmap {
		val width = bitmap.width
		val height = bitmap.height
		val boundingBox = detection.locationData.relativeBoundingBox
		val left = ((boundingBox.xmin) * width).toInt().coerceIn(0, width)
		val top = ((boundingBox.ymin) * height).toInt().coerceIn(0, height)
		val right = ((boundingBox.width) * width).toInt().coerceAtMost(width - left)
		val bottom = ((boundingBox.height) * height).toInt().coerceAtMost(height - top)
		val leftEye = detection.locationData.getRelativeKeypoints(FaceKeypoint.LEFT_EYE)
		val rightEye = detection.locationData.getRelativeKeypoints(FaceKeypoint.RIGHT_EYE)
		val lx = leftEye.x * width
		val ly = leftEye.y * height
		val rx = rightEye.x * width
		val ry = rightEye.y * height
		val dY = ry - ly
		val dX = rx - lx
		val eyesCenter = PointF((lx + rx) / 2, (ly + ry) / 2)
		val angle = (Math.toDegrees(atan2(dY, dX).toDouble())) - 245
		val matrix = Matrix()
		matrix.setRotate(angle.toFloat(), eyesCenter.x, eyesCenter.y)
		return Bitmap.createBitmap(bitmap, left, top, right, bottom, matrix, true)
	}*/
}

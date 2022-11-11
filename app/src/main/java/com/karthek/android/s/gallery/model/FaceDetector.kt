package com.karthek.android.s.gallery.model

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.formats.proto.DetectionProto.Detection
import com.google.mediapipe.solutions.facedetection.FaceDetection
import com.google.mediapipe.solutions.facedetection.FaceDetectionOptions
import com.google.mediapipe.solutions.facedetection.FaceDetectionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class FaceDetector @Inject constructor(@ApplicationContext context: Context) {
	private val faceDetection: FaceDetection

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
		detectionList.forEach { detection -> faceBitmaps.add(getFaceBitmap(bitmap, detection)) }
		return faceBitmaps
	}

	private fun getFaceBitmap(bitmap: Bitmap, detection: Detection): Bitmap {
		val width = bitmap.width
		val height = bitmap.height
		val boundingBox = detection.locationData.relativeBoundingBox
		val left = (boundingBox.xmin) * width
		val top = (boundingBox.ymin) * height
		val right = (boundingBox.width) * width
		val bottom = (boundingBox.height) * height
		return Bitmap.createBitmap(bitmap, left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
	}
}
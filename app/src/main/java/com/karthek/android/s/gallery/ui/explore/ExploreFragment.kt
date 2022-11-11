package com.karthek.android.s.gallery.ui.explore

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.mediapipe.formats.proto.DetectionProto
import com.google.mediapipe.solutions.facedetection.FaceDetection
import com.google.mediapipe.solutions.facedetection.FaceDetectionOptions
import com.google.mediapipe.solutions.facedetection.FaceDetectionResult
import com.karthek.android.s.gallery.databinding.FragmentExploreBinding
import com.karthek.android.s.gallery.ml.FacenetMobileV1
import com.karthek.android.s.gallery.ml.ImageSceneUint81
import com.karthek.android.s.gallery.model.Classify
import com.karthek.android.s.gallery.model.FaceDetector
import com.karthek.android.s.gallery.model.FaceRecognizer
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import org.tensorflow.lite.task.vision.classifier.ImageClassifier.ImageClassifierOptions
import java.io.IOException
import java.util.*

class ExploreFragment : Fragment() {
	private var binding: FragmentExploreBinding? = null
	private var exploreViewModel: ExploreViewModel? = null
	var uri: Uri? = null
	private var mGetContent =
		registerForActivityResult<String, Uri>(ActivityResultContracts.GetContent()) { uri: Uri? ->
			// Handle the returned Uri
			this.uri = uri
			binding!!.imageView6.setImageURI(uri)
		}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		exploreViewModel = ViewModelProvider(this)[ExploreViewModel::class.java]
		binding = FragmentExploreBinding.inflate(inflater, container, false)
		exploreViewModel!!.text.observe(viewLifecycleOwner) { s: String? ->
			binding!!.textDashboard.text = s
		}
		return binding!!.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding!!.imageView6.setOnClickListener { mGetContent.launch("image/*") }
		binding!!.button.setOnClickListener { runClassifier() }
	}

	private fun runClassifier() {
		if (uri == null) return
		//ml_kit();
		//tf_lite();
		//tf_lite_task();
		//classii();
		//obj_det();
		Thread {
			faceDetect()
			faceRecognize2()
		}.start()
	}

	private fun getBitmap(w: Int, h: Int): Bitmap? {
		var bitmap: Bitmap? = null
		try {
			bitmap = requireActivity().applicationContext.contentResolver.loadThumbnail(uri!!,
				Size(w, h), null)
		} catch (e: IOException) {
			e.printStackTrace()
		}
		return bitmap
	}

	private fun classii() {
		val classify = Classify(requireContext())
		binding!!.textDashboard.text = classify.getCategory(getBitmap(224, 224)!!)
	}

	private fun tfLite() {
		val options = Model.Options.Builder().setDevice(Model.Device.NNAPI).build()
		try {
			val model = ImageSceneUint81.newInstance(requireContext(), options)

			// Creates inputs for reference.
			val image = TensorImage.fromBitmap(getBitmap(224, 224))

			// Runs model inference and gets result.
			val outputs = model.process(image)
			val probability = outputs.probabilityAsCategoryList
			probability.filter { category: Category -> category.score != 0f }
			probability.sortWith { o1: Category, o2: Category ->
				o2.score.compareTo(o1.score)
			}
			val s = StringBuilder("Inference:\n\n")
			for (c in probability) {
				s.append(String.format(Locale.ENGLISH, "%-20s %f\n", c.label, c.score))
			}
			binding!!.textDashboard.text = s
			// Releases model resources if no longer used.
			model.close()
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	//	private void obj_det() {
	//		try {
	//			EfficientdetLite01 model = EfficientdetLite01.newInstance(requireContext());
	//
	//			// Creates inputs for reference.
	//			TensorImage image = TensorImage.fromBitmap(getBitmap(224,224));
	//
	//			// Runs model inference and gets result.
	//			EfficientdetLite01.Outputs outputs = model.process(image);
	//			EfficientdetLite01.DetectionResult detectionResult = outputs.getDetectionResultList().get(0);
	//
	//			// Gets result from DetectionResult.
	//			RectF location = detectionResult.getLocationAsRectF();
	//			String category = detectionResult.getCategoryAsString();
	//			float score = detectionResult.getScoreAsFloat();
	//
	//			@NonNull List<EfficientdetLite01.DetectionResult> probability = outputs.getDetectionResultList();
	//			StringBuilder s = new StringBuilder("Inference:\n\n");
	//			for (EfficientdetLite01.DetectionResult c : probability) {
	//				s.append(String.format(Locale.ENGLISH, "%-20s %f\n",
	//						c.getCategoryAsString(),
	//						c.getScoreAsFloat()));
	//			}
	//			binding.textDashboard.setText(s);
	//
	//			// Releases model resources if no longer used.
	//			model.close();
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//
	//	}
	private fun faceDetect() {
		val faceDetectionOptions = FaceDetectionOptions.builder()
			.setStaticImageMode(true)
			.setModelSelection(1)
			.build()
		val faceDetection = FaceDetection(requireContext(), faceDetectionOptions)
		val bitmap = getBitmap(192, 192)
		faceDetection.send(bitmap)
		faceDetection.setResultListener { result: FaceDetectionResult ->
			val detectionList: List<DetectionProto.Detection> = result.multiFaceDetections()
			Log.v("faced", "got res " + detectionList.size)
			if (detectionList.isEmpty()) {
				return@setResultListener
			}
			val width = result.inputBitmap().width
			val height = result.inputBitmap().height
			val bitmap1 = bitmap!!.copy(bitmap.config, true)
			val canvas = Canvas(bitmap1)
			for (detection in detectionList) {
				drawBoundingBoxes(detection, canvas, width, height)
			}
			val detection = detectionList[0]
			val left = detection.locationData.relativeBoundingBox.xmin * width
			val top = detection.locationData.relativeBoundingBox.ymin * height
			val right = detection.locationData.relativeBoundingBox.width * width
			val bottom = detection.locationData.relativeBoundingBox.height * height
			val croppedBitmap = Bitmap.createBitmap(bitmap,
				left.toInt(),
				top.toInt(),
				right.toInt(),
				bottom.toInt())
			faceRecognize(croppedBitmap)
			Log.v("faced", String.format("got %f %f %f %f", left, top, right, bottom))
			requireActivity().runOnUiThread {
				binding!!.imageView6.setImageBitmap(bitmap1)
				binding!!.textDashboard.text = String.format(Locale.ENGLISH,
					"detceted faces: %d",
					detectionList.size
				)
			}
		}
	}

	private fun faceRecognize(bitmap: Bitmap) {
		try {
			val model = FacenetMobileV1.newInstance(requireContext())

			// Creates inputs for reference.
			val image = TensorImage.fromBitmap(bitmap)
			val processor = ImageProcessor.Builder()
				.add(ResizeOp(112, 112, ResizeOp.ResizeMethod.BILINEAR))
				.build()
			val processedImage = processor.process(image)

			// Runs model inference and gets result.
			val outputs = model.process(processedImage)
			val embedding = outputs.embeddingAsTensorBuffer
			val floatArray = embedding.floatArray
			Log.v("recog", "size: " + floatArray.size + Arrays.toString(embedding.floatArray))
			// Releases model resources if no longer used.
			model.close()
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	private fun faceRecognize2() {
		val faceRecognizer = FaceRecognizer(requireContext(), FaceDetector(requireContext()))
		lifecycleScope.launch {
			getBitmap(192, 192)?.let {
				Log.v("recog2", faceRecognizer.getFaceEmbeddings(it)[0].contentToString())
			}
		}
	}

	private fun drawBoundingBoxes(
		detection: DetectionProto.Detection, canvas: Canvas, width: Int,
		height: Int,
	) {
		Log.v("faced", "drwing on canvas")
		val bboxPaint = Paint()
		bboxPaint.color = Color.GREEN
		bboxPaint.style = Paint.Style.STROKE
		bboxPaint.strokeWidth = 5f
		val left = detection.locationData.relativeBoundingBox.xmin * width
		val top = detection.locationData.relativeBoundingBox.ymin * height
		val right = left + detection.locationData.relativeBoundingBox.width * width
		val bottom = top + detection.locationData.relativeBoundingBox.height * height
		canvas.drawRect(left, top, right, bottom, bboxPaint)
	}

	private fun tfLiteTask() {
		try {
			val bitmap = getBitmap(224, 224)
			val image = TensorImage.fromBitmap(bitmap)
			// Initialization
			val options = ImageClassifierOptions.builder()
				.setBaseOptions(BaseOptions.builder()
					.build())
				.setMaxResults(5)
				.build()
			val imageClassifier = ImageClassifier.createFromFileAndOptions(requireContext(),
				"image_scene_uint8_1.tflite",
				options)

			// Run inference
			val results = imageClassifier.classify(image)
			val probability = results[0].categories
			val s = StringBuilder("Inference:\n\n")
			for (c in probability) {
				s.append(String.format(Locale.ENGLISH, "%-20s %f\n", c.label, c.score))
			}
			binding!!.textDashboard.text = s
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}
}
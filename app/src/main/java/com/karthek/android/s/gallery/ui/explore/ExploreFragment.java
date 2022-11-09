package com.karthek.android.s.gallery.ui.explore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.mediapipe.formats.proto.DetectionProto;
import com.google.mediapipe.solutions.facedetection.FaceDetection;
import com.google.mediapipe.solutions.facedetection.FaceDetectionOptions;
import com.karthek.android.s.gallery.databinding.FragmentExploreBinding;
import com.karthek.android.s.gallery.ml.ImageSceneUint81;
import com.karthek.android.s.gallery.model.Classify;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.vision.classifier.Classifications;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ExploreFragment extends Fragment {

	private FragmentExploreBinding binding;
	private ExploreViewModel exploreViewModel;
	Uri uri;
	ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
		// Handle the returned Uri
		this.uri = uri;
		binding.imageView6.setImageURI(uri);
	});

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		exploreViewModel = new ViewModelProvider(this).get(ExploreViewModel.class);
		binding = FragmentExploreBinding.inflate(inflater, container, false);
		exploreViewModel.getText().observe(getViewLifecycleOwner(), s -> binding.textDashboard.setText(s));
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.imageView6.setOnClickListener(v -> mGetContent.launch("image/*"));
		binding.button.setOnClickListener((v) -> runClassifier());
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);

	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	private void runClassifier() {
		if (uri == null) return;
		//ml_kit();
		//tf_lite();
		//tf_lite_task();
		//classii();
		//obj_det();
		new Thread(this::face_detect).start();
	}

	private Bitmap getBitmap(int w, int h) {
		Bitmap bitmap = null;
		try {
			bitmap = requireActivity().getApplicationContext().getContentResolver().loadThumbnail(uri,
					new Size(w, h), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	private void classii() {
		Classify classify = new Classify(requireContext());
		binding.textDashboard.setText(classify.getCategory(getBitmap(224, 224)));
	}

	private void tf_lite() {
		Model.Options options = new Model.Options.Builder().setDevice(Model.Device.NNAPI).build();
		try {
			ImageSceneUint81 model = ImageSceneUint81.newInstance(requireContext(), options);

			// Creates inputs for reference.
			TensorImage image = TensorImage.fromBitmap(getBitmap(224, 224));

			// Runs model inference and gets result.
			ImageSceneUint81.Outputs outputs = model.process(image);
			List<Category> probability = outputs.getProbabilityAsCategoryList();

			probability.removeIf(category -> category.getScore() == 0);
			probability.sort((o1, o2) -> Float.compare(o2.getScore(), o1.getScore()));
			StringBuilder s = new StringBuilder("Inference:\n\n");
			for (Category c : probability) {
				s.append(String.format(Locale.ENGLISH, "%-20s %f\n", c.getLabel(), c.getScore()));
			}
			binding.textDashboard.setText(s);
			// Releases model resources if no longer used.
			model.close();
		} catch (IOException e) {
			e.printStackTrace();
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

	private void face_detect() {
		FaceDetectionOptions faceDetectionOptions =
				FaceDetectionOptions.builder()
						.setStaticImageMode(true)
						.setModelSelection(1)
						.build();
		FaceDetection faceDetection = new FaceDetection(requireContext(), faceDetectionOptions);
		Bitmap bitmap = getBitmap(192, 192);
		faceDetection.send(bitmap);
		faceDetection.setResultListener(result -> {
			List<DetectionProto.Detection> detectionList = result.multiFaceDetections();
			Log.v("faced", "got res " + detectionList.size());
			if (detectionList.isEmpty()) {
				return;
			}
			int width = result.inputBitmap().getWidth();
			int height = result.inputBitmap().getHeight();
			Bitmap bitmap1=bitmap.copy(bitmap.getConfig(),true);
			Canvas canvas = new Canvas(bitmap1);
			for (DetectionProto.Detection detection : detectionList) {
				drawBoundingBoxes(detection, canvas, width, height);
			}
			DetectionProto.Detection detection = detectionList.get(0);
			float left = detection.getLocationData().getRelativeBoundingBox().getXmin() * width;
			float top = detection.getLocationData().getRelativeBoundingBox().getYmin() * height;
			float right = detection.getLocationData().getRelativeBoundingBox().getWidth() * width;
			float bottom = detection.getLocationData().getRelativeBoundingBox().getHeight() * height;
//			Bitmap croppedBitmap = Bitmap.createBitmap(finalBitmap, (int) left, (int) top,
//					(int) right, (int) bottom);
			Log.v("faced", String.format("got %f %f %f %f", left, top, right, bottom));
			requireActivity().runOnUiThread(() -> {
						binding.imageView6.setImageBitmap(bitmap1);
						binding.textDashboard.setText(String.format(Locale.ENGLISH,
								"detceted faces: %d",
								detectionList.size()
						));
					}
			);
		});
	}

	private void drawBoundingBoxes(DetectionProto.Detection detection, Canvas canvas, int width,
								   int height) {
		Log.v("faced", "drwing on canvas");
		Paint bboxPaint = new Paint();
		bboxPaint.setColor(Color.GREEN);
		bboxPaint.setStyle(Paint.Style.STROKE);
		bboxPaint.setStrokeWidth(5);
		float left = detection.getLocationData().getRelativeBoundingBox().getXmin() * width;
		float top = detection.getLocationData().getRelativeBoundingBox().getYmin() * height;
		float right = left + detection.getLocationData().getRelativeBoundingBox().getWidth() * width;
		float bottom = top + detection.getLocationData().getRelativeBoundingBox().getHeight() * height;
		canvas.drawRect(left, top, right, bottom, bboxPaint);
	}

	private void tf_lite_task() {
		try {

			Bitmap bitmap = getBitmap(224, 224);
			TensorImage image = TensorImage.fromBitmap(bitmap);
			// Initialization
			ImageClassifier.ImageClassifierOptions options =
					ImageClassifier.ImageClassifierOptions.builder()
							.setBaseOptions(BaseOptions.builder()
									.build())
							.setMaxResults(5)
							.build();
			ImageClassifier imageClassifier = ImageClassifier.createFromFileAndOptions(requireContext(), "image_scene_uint8_1.tflite", options);

			// Run inference
			List<Classifications> results = imageClassifier.classify(image);
			List<Category> probability = results.get(0).getCategories();
			StringBuilder s = new StringBuilder("Inference:\n\n");
			for (Category c : probability) {
				s.append(String.format(Locale.ENGLISH, "%-20s %f\n", c.getLabel(), c.getScore()));
			}
			binding.textDashboard.setText(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
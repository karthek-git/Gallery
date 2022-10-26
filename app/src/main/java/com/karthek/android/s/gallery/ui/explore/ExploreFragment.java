package com.karthek.android.s.gallery.ui.explore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
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

import com.karthek.android.s.gallery.databinding.FragmentExploreBinding;
import com.karthek.android.s.gallery.ml.EfficientdetLite01;
import com.karthek.android.s.gallery.ml.EfficientnetLite0Int82;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.model.Model;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ExploreFragment extends Fragment {

	private FragmentExploreBinding binding;
	private ExploreViewModel exploreViewModel;
	Uri uri;
	ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
			uri -> {
				// Handle the returned Uri
				this.uri = uri;
				binding.imageView6.setImageURI(uri);
			});

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		exploreViewModel =
				new ViewModelProvider(this).get(ExploreViewModel.class);
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
		tf_lite();
		//tf_lite_task();
		//obj_det();
	}

	private Bitmap getBitmap() throws IOException {
		return requireActivity().getApplicationContext().getContentResolver().loadThumbnail(uri,
				new Size(224, 224), null);
	}

	private void tf_lite() {
		Model.Options options = new Model.Options.Builder().setDevice(Model.Device.NNAPI).build();
		try {
			EfficientnetLite0Int82 model = EfficientnetLite0Int82.newInstance(requireContext(),options);

			// Creates inputs for reference.
			TensorImage image = TensorImage.fromBitmap(getBitmap());

			// Runs model inference and gets result.
			EfficientnetLite0Int82.Outputs outputs = model.process(image);
			List<Category> probability = outputs.getProbabilityAsCategoryList();

			probability.removeIf(category -> category.getScore() == 0);
			probability.sort((o1, o2) -> Float.compare(o2.getScore(), o1.getScore()));
			StringBuilder s = new StringBuilder("Inference:\n\n");
			for (Category c : probability) {
				s.append(String.format(Locale.ENGLISH, "%-20s %f\n",
						c.getLabel(),
						c.getScore()));
			}
			binding.textDashboard.setText(s);
			// Releases model resources if no longer used.
			model.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void obj_det() {
		try {
			EfficientdetLite01 model = EfficientdetLite01.newInstance(requireContext());

			// Creates inputs for reference.
			TensorImage image = TensorImage.fromBitmap(getBitmap());

			// Runs model inference and gets result.
			EfficientdetLite01.Outputs outputs = model.process(image);
			EfficientdetLite01.DetectionResult detectionResult = outputs.getDetectionResultList().get(0);

			// Gets result from DetectionResult.
			RectF location = detectionResult.getLocationAsRectF();
			String category = detectionResult.getCategoryAsString();
			float score = detectionResult.getScoreAsFloat();

			@NonNull List<EfficientdetLite01.DetectionResult> probability = outputs.getDetectionResultList();
			StringBuilder s = new StringBuilder("Inference:\n\n");
			for (EfficientdetLite01.DetectionResult c : probability) {
				s.append(String.format(Locale.ENGLISH, "%-20s %f\n",
						c.getCategoryAsString(),
						c.getScoreAsFloat()));
			}
			binding.textDashboard.setText(s);

			// Releases model resources if no longer used.
			model.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void face_detect() {

	}

/*	private void tf_lite_task() {
		try {

			Bitmap bitmap = getBitmap();
			TensorImage image = TensorImage.fromBitmap(bitmap);
			// Initialization
			ImageClassifier.ImageClassifierOptions options = ImageClassifier.ImageClassifierOptions.builder().setMaxResults(1).build();
			ImageClassifier imageClassifier =
					ImageClassifier.createFromFileAndOptions(
							requireContext(),
							"mobilenet_v1_1.0_224_quantized_1_metadata_1.tflite",
							options);

			// Run inference
			List<Classifications> results = imageClassifier.Classify(image);
			List<Category> probability = results.get(0).getCategories();
			StringBuilder s = new StringBuilder("Inference:\n\n");
			for (Category c : probability) {
				s.append(String.format(Locale.ENGLISH, "%-20s %f\n",
						c.getLabel(),
						c.getScore()));
			}
			binding.textDashboard.setText(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

/*	private void ml_kit() {
		InputImage image = null;
		try {
			image = InputImage.fromFilePath(requireContext(), uri);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
		if (image == null) return;
		labeler.process(image).addOnSuccessListener(imageLabels -> {
			StringBuilder s = new StringBuilder("Inference:\n\n");
			for (ImageLabel imageLabel : imageLabels) {
				s.append(String.format(Locale.ENGLISH, "%-20s %f\n",
						imageLabel.getText(),
						imageLabel.getConfidence()));
			}
			binding.textDashboard.setText(s);
		});
	}*/
}
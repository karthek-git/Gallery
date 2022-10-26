package com.karthek.android.s.gallery.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class FaceDetectionView extends View {
	private static final int MAX_FACES = 10;
	private Bitmap background_image;
	private FaceDetector.Face[] faces;
	private int face_count;

	// preallocate for onDraw(...)
	private final PointF tmp_point = new PointF();
	private final Paint tmp_paint = new Paint();

	public FaceDetectionView(Context context) {
		super(context);
	}

	public FaceDetectionView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);

	}

	public void updateImage(Bitmap image_fn) {
		background_image = image_fn;
		FaceDetector face_detector = new FaceDetector(
				background_image.getWidth(), background_image.getHeight(),
				MAX_FACES);

		faces = new FaceDetector.Face[MAX_FACES];
		// The bitmap must be in 565 format (for now).
		face_count = face_detector.findFaces(background_image, faces);
		Log.d("Face_Detection", "Face Count: " + face_count);
	}

	public void onDraw(Canvas canvas) {
		if (background_image == null) return;
		canvas.drawBitmap(background_image, 0, 0, null);
		for (int i = 0; i < face_count; i++) {
			FaceDetector.Face face = faces[i];
			tmp_paint.setColor(Color.RED);
			tmp_paint.setAlpha(100);
			face.getMidPoint(tmp_point);
			canvas.drawCircle(tmp_point.x, tmp_point.y, face.eyesDistance(),
					tmp_paint);
		}
	}
}

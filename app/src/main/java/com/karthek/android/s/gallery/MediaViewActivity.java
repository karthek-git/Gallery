package com.karthek.android.s.gallery;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.SeekBar;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.karthek.android.s.gallery.databinding.ActivityMediaViewBinding;

import java.util.concurrent.TimeUnit;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MediaViewActivity extends AppCompatActivity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * Some older devices needs a small delay between UI widget updates
	 * and a change of the status and navigation bar.
	 */
	private static final int UI_ANIMATION_DELAY = 300;
	private final Handler mHideHandler = new Handler();
	private View mContentView;
	private final Runnable mHidePart2Runnable = new Runnable() {
		@SuppressLint("InlinedApi")
		@Override
		public void run() {
			// Delayed removal of status and navigation bar

			// Note that some of these constants are new as of API 16 (Jelly Bean)
			// and API 19 (KitKat). It is safe to use them, as they are inlined
			// at compile-time and do nothing on earlier devices.
			mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	};
	private View mControlsView;
	private final Runnable mShowPart2Runnable = new Runnable() {
		@Override
		public void run() {
			// Delayed display of UI elements
			ActionBar actionBar = getSupportActionBar();
			if (actionBar != null) {
				actionBar.show();
			}
			mControlsView.setVisibility(View.VISIBLE);
		}
	};
	private boolean mVisible;
	private final Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			hide();
		}
	};
	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (AUTO_HIDE) {
						delayedHide(AUTO_HIDE_DELAY_MILLIS);
					}
					break;
				case MotionEvent.ACTION_UP:
					view.performClick();
					break;
				default:
					break;
			}
			return false;
		}
	};
	private ActivityMediaViewBinding binding;
	private int duration;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = ActivityMediaViewBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		mVisible = true;
		mControlsView = binding.fullscreenContentControls;

		Intent intent = getIntent();
		if (intent.getType().startsWith("image/")) {
			mContentView = binding.imageView;
			binding.imageView.setImage(ImageSource.uri(intent.getData()));
		} else {
			mContentView = binding.videoView;
			binding.imageView.setVisibility(View.GONE);
			binding.video.setVisibility(View.VISIBLE);
			binding.videoView.setVideoURI(intent.getData());
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
			toggle();
			binding.videoView.start();
			binding.videoView.setOnClickListener(v -> {
				VideoView videoView = (VideoView) v;
				if (videoView.isPlaying())
					videoView.pause();
				else
					videoView.start();
			});
			binding.videoView.setOnPreparedListener(mp -> {
				duration = mp.getDuration();
				binding.remainingTime.setText(String.valueOf(TimeUnit.MILLISECONDS.toMinutes(duration)));
			});
			binding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					int p = (progress * duration) / 100;
					binding.videoView.seekTo(p);
					binding.currentTime.setText(String.valueOf(TimeUnit.MILLISECONDS.toMinutes(p)));
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {

				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {

				}
			});

			ScaleGestureDetector scaleGestureDetector =
					new ScaleGestureDetector(binding.videoView.getContext(), new ScaleMediaView());
			binding.videoView.setOnTouchListener((v, event) -> {
				scaleGestureDetector.onTouchEvent(event);
				return event.getPointerCount() > 1;
			});
		}


		// Set up the user interaction to manually show or hide the system UI.
/*
		mContentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				toggle();
			}
		});
*/

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		binding.buttonActionShare.setOnTouchListener(mDelayHideTouchListener);
	}


	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		enterPictureInPictureMode();
	}

	private void toggle() {
		if (mVisible) {
			hide();
		} else {
			show();
		}
	}

	private void hide() {
		// Hide UI first
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}
		mControlsView.setVisibility(View.GONE);
		mVisible = false;

		// Schedule a runnable to remove the status and navigation bar after a delay
		mHideHandler.removeCallbacks(mShowPart2Runnable);
		mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
	}

	private void show() {
		// Show the system bar
		mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
		mVisible = true;

		// Schedule a runnable to display UI elements after a delay
		mHideHandler.removeCallbacks(mHidePart2Runnable);
		mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
	}

	/**
	 * Schedules a call to hide() in delay milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	private float scaleFactor = 1.0f;

	private class ScaleMediaView extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			scaleFactor *= detector.getScaleFactor();
			scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 10.0f));
			binding.videoView.setScaleX(scaleFactor);
			binding.videoView.setScaleY(scaleFactor);
			return true;
		}
	}
}
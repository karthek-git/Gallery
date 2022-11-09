package com.karthek.android.s.gallery.ui.players;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.karthek.android.s.gallery.R;
import com.karthek.android.s.gallery.databinding.ActivityVideoPlayerBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Objects;

public class VideoPlayerActivity extends AppCompatActivity {

	ActivityVideoPlayerBinding binding;
	SimpleExoPlayer simpleExoPlayer;
	VideoPlayerActivityViewModel viewModel;
	int hide_flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
			| View.SYSTEM_UI_FLAG_FULLSCREEN
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_video_player);
		binding.videoPlayerView.setSystemUiVisibility(hide_flags);
		viewModel = new ViewModelProvider(this).get(VideoPlayerActivityViewModel.class);
		binding.setViewModel(viewModel);
		simpleExoPlayer = new SimpleExoPlayer.Builder(this).build();
		binding.videoPlayerView.setPlayer(simpleExoPlayer);
		MediaItem mediaItem = MediaItem.fromUri(getIntent().getData());
		simpleExoPlayer.setMediaItem(mediaItem);
		simpleExoPlayer.prepare();
		simpleExoPlayer.play();
		((TextView) findViewById(R.id.video_name)).setText(getVideoName());
	}

	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		enterPictureInPictureMode();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		simpleExoPlayer.release();
		simpleExoPlayer = null;
	}


	@Override
	public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
		super.onPictureInPictureModeChanged(isInPictureInPictureMode);
		if (isInPictureInPictureMode) binding.videoPlayerView.hideController();
	}

	public boolean videoViewClickHandler(View v, MotionEvent e) {
		if (viewModel.controlsLocked.getValue() && binding.controllerLockOpen.getVisibility() != View.VISIBLE) {
			binding.controllerLockOpen.setVisibility(View.VISIBLE);
			binding.controllerLockOpen.postDelayed(() -> binding.controllerLockOpen.setVisibility(View.GONE), 3000);
		}
		return false;
	}

	public void toggle_mute(View view) {
		if (viewModel.audioMuted.getValue()) {
			simpleExoPlayer.setVolume(1);
			viewModel.audioMuted.setValue(false);
			((ImageView) view).setImageResource(R.drawable.ic_volume_full);
		} else {
			simpleExoPlayer.setVolume(0);
			viewModel.audioMuted.setValue(true);
			((ImageView) view).setImageResource(R.drawable.ic_volume_muted);
		}
	}

	@SuppressLint("WrongConstant")
	public void control_fullscreen(View view) {
		int r = viewModel.resizeMode.getValue();
		r = r < 4 ? r + 1 : 0;
		binding.videoPlayerView.setResizeMode(r);
		viewModel.resizeMode.setValue(r);
	}


	public void enter_pip(View view) {
		enterPictureInPictureMode();
	}

	public void toggle_controller_lock(View view) {
		if (viewModel.controlsLocked.getValue()) {
			binding.controllerLockOpen.setVisibility(View.GONE);
			binding.videoPlayerView.setUseController(true);
			viewModel.controlsLocked.setValue(false);
			binding.videoPlayerView.setOnTouchListener(null);
			binding.videoPlayerView.showController();
		} else {
			binding.videoPlayerView.setUseController(false);
			viewModel.controlsLocked.setValue(true);
			binding.videoPlayerView.setOnTouchListener(this::videoViewClickHandler);
		}
	}


	public void capture_frame(View view) {
		long pos = simpleExoPlayer.getCurrentPosition();
		new Thread(() -> capture_frame(pos)).start();
	}

	private void capture_frame(long pos) {
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		retriever.setDataSource(this, getIntent().getData());
		Bitmap bitmap = retriever.getFrameAtTime(pos, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
		FileOutputStream o = null;
		try {
			o = getFileOutputStream(pos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (o != null)
			bitmap.compress(Bitmap.CompressFormat.WEBP, 100, o);

	}

	private String getVideoName() {
		if (viewModel.name.getValue() == null) {
			String name = null;
			String[] projection = {
					OpenableColumns.DISPLAY_NAME
			};
			Cursor cursor = getApplicationContext().getContentResolver().query(getIntent().getData(),
					projection,
					null, null, null);
			if (cursor != null) {
				cursor.moveToNext();
				name = cursor.getString(cursor.getColumnIndex(projection[0]));
				cursor.close();
			}
			if (name != null) {
				int i = name.lastIndexOf('.');
				name = name.substring(0, i);
			}
			viewModel.name.setValue(name);
		}
		return viewModel.name.getValue();
	}

	private FileOutputStream getFileOutputStream(long millis) throws FileNotFoundException {
		File file =
				new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) +
						"/VideoSnapshots/" + getVideoName() + millis + ".webp");
		Objects.requireNonNull(file.getParentFile()).mkdirs();
		return new FileOutputStream(file);
	}
}
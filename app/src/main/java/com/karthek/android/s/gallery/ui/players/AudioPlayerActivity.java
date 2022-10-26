package com.karthek.android.s.gallery.ui.players;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.karthek.android.s.gallery.R;
import com.karthek.android.s.gallery.databinding.ActivityAudioPlayerBinding;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class AudioPlayerActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener {

    ActivityAudioPlayerBinding binding;
    MediaPlayer mediaPlayer;
    int duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setGravity(Gravity.BOTTOM);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_audio_player);
        Intent intent = getIntent();
        Log.v("aud", "uri:" + intent.getData());
        new Thread(this::setAudioInfo).start();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), intent.getData());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        binding.seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    Timer timer;


    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        duration = mp.getDuration();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    float pos = mediaPlayer.getCurrentPosition();
                    binding.seekBar.post(() -> binding.seekBar.setProgress((int) ((pos / duration) * 100)));
                }
            }
        }, 0, 1000);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser)
            mediaPlayer.seekTo((progress * duration) / 100);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        binding.seekBar.setProgress(100);
        binding.audioPlayButton.setImageResource(R.drawable.exo_controls_play);
    }

    public void toggle(View view) {
        if (mediaPlayer.isPlaying()) {
            binding.audioPlayButton.setImageResource(R.drawable.ic_play_button);
            binding.getRoot().setKeepScreenOn(false);
            mediaPlayer.pause();
        } else {
            binding.audioPlayButton.setImageResource(R.drawable.ic_pause_button);
            binding.getRoot().setKeepScreenOn(true);
            mediaPlayer.start();
        }
    }

    private void setAudioInfo() {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(this, getIntent().getData());
        String name =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if (name == null)
            name = getAudioName();
        if (name != null) {
            String finalName = name;
            runOnUiThread(() -> binding.audioTrackName.setText(finalName));
        }
        byte[] art = mediaMetadataRetriever.getEmbeddedPicture();
        if (art != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            runOnUiThread(() -> {
                binding.audioTrackArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
                binding.audioTrackArt.setImageBitmap(bitmap);
            });
        }
    }

    private String getAudioName() {
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
        return name;
    }


}
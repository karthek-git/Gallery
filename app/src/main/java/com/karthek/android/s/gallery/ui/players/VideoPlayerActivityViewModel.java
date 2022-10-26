package com.karthek.android.s.gallery.ui.players;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class VideoPlayerActivityViewModel extends ViewModel {
	public MutableLiveData<Integer> resizeMode = new MutableLiveData<>();
	public MutableLiveData<Boolean> audioMuted = new MutableLiveData<>();
	public MutableLiveData<Boolean> controlsLocked = new MutableLiveData<>();
	public MutableLiveData<String> name = new MutableLiveData<>();

	public VideoPlayerActivityViewModel() {
		audioMuted.setValue(false);
		resizeMode.setValue(0);
		controlsLocked.setValue(false);
	}

	public void setVideoName(String n){
		name.setValue(n);
	}
}

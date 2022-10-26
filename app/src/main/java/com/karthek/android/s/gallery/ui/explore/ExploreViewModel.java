package com.karthek.android.s.gallery.ui.explore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExploreViewModel extends ViewModel {

	private MutableLiveData<String> mText;

	public ExploreViewModel() {
		mText = new MutableLiveData<>();
		mText.setValue("Inference:\n\n");
	}

	public LiveData<String> getText() {
		return mText;
	}
}
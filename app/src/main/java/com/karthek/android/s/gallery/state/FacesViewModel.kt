package com.karthek.android.s.gallery.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthek.android.s.gallery.state.db.SFaceWithSMedia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FacesViewModel @Inject constructor(private val repo: SMediaAccess) : ViewModel() {
	var sFacesWithSMedia by mutableStateOf<List<SFaceWithSMedia>?>(null)

	init {
		viewModelScope.launch {
			sFacesWithSMedia = repo.getSFacesWithSMedia()
		}
	}
}
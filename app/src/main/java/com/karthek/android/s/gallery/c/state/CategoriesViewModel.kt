package com.karthek.android.s.gallery.c.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthek.android.s.gallery.state.db.SCategoryWithSMedia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(private val repo: SMediaAccess) : ViewModel() {
	var sCategoriesWithSMedia by mutableStateOf<List<SCategoryWithSMedia>?>(null)

	init {
		viewModelScope.launch {
			sCategoriesWithSMedia = repo.getSCategoriesWithSMedia()
				.filter { sCategoryWithSMedia -> sCategoryWithSMedia.SMediaList.isNotEmpty() }
		}
	}
}
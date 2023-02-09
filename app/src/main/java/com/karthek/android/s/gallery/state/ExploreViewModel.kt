package com.karthek.android.s.gallery.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthek.android.s.gallery.state.db.SMedia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(private val repo: SMediaAccess) : ViewModel() {
	var searchInProgress by mutableStateOf(false)
	var searchResultSMedia by mutableStateOf<List<SMedia>?>(null)

	fun onSearchAction(query: String) {
		viewModelScope.launch {
			searchInProgress = true
			searchResultSMedia = withContext(Dispatchers.IO) {
				repo.searchSMedia(query)
			}
			searchInProgress = false
		}
	}

}
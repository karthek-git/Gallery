package com.karthek.android.s.gallery.c.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthek.android.s.gallery.c.a.MFolder
import com.karthek.android.s.gallery.state.db.SMedia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.SoftReference
import javax.inject.Inject

@HiltViewModel
class SMViewModel @Inject constructor(private val repo: SMediaAccess) : ViewModel() {
	var sMediaList by mutableStateOf<List<SMedia>?>(null)
	var folderList by mutableStateOf<List<MFolder>?>(null)
	var searchInProgress by mutableStateOf(false)
	var currentSMediaList by mutableStateOf<List<SMedia>?>(null)
	var currentSMedia: SMedia? = null

	fun getFolderContents(index: Int) {
		val folder = folderList!![index]
		if (folder.l.value == null) {
			currentSMediaList = null
			viewModelScope.launch {
				folder.l.value =
					withContext(Dispatchers.Default) { SoftReference(repo.getSMedia(folder.path)) }
				currentSMediaList = folder.l.value?.get()
			}
		} else {
			currentSMediaList = folder.l.value?.get()
		}
	}

	fun onSearchAction(query: String) {
		viewModelScope.launch {
			searchInProgress = true
			currentSMediaList = null
			currentSMediaList = withContext(Dispatchers.IO) {
				repo.searchSMedia(query.trim())
			}
			searchInProgress = false
		}
	}

	init {
		viewModelScope.launch {
			sMediaList = repo.getSMedia()

			folderList = repo.getFolders()
		}
	}
}
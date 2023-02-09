package com.karthek.android.s.gallery.state

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthek.android.s.gallery.a.SMediaFolder
import com.karthek.android.s.gallery.state.db.SMedia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.SoftReference
import javax.inject.Inject

@HiltViewModel
class SMViewModel @Inject constructor(private val repo: SMediaAccess) : ViewModel() {
	var sMediaList = SMediaStateList()
	var folderList by mutableStateOf<List<SMediaFolder>?>(null)
	var currentSMediaList = SMediaStateList()
	var currentSMedia: SMedia? = null

	fun getFolderContents(index: Int) {
		val folder = folderList!![index]
		folder.l.value?.get()?.let {
			currentSMediaList.list = it
			return
		}
		viewModelScope.launch {
			currentSMediaList.isLoading.value = true
			folder.l.value = withContext(Dispatchers.Default) {
				SoftReference(repo.getSMedia(folder.path).toMutableStateList())
			}
			folder.l.value?.get()?.let { currentSMediaList.list = it }
			currentSMediaList.isLoading.value = false
		}
	}

	fun onSearchAction(query: String) {
		viewModelScope.launch {
			currentSMediaList.isLoading.value = true
			currentSMediaList.list = withContext(Dispatchers.IO) {
				repo.searchSMedia(query.trim())
			}.toMutableStateList()
			currentSMediaList.isLoading.value = false
		}
	}

	init {
		viewModelScope.launch {
			sMediaList.isLoading.value = true
			sMediaList.list = repo.getSMedia().toMutableStateList()
			sMediaList.isLoading.value = false

			folderList = repo.getFolders()
		}
	}
}

data class SMediaStateList(
	var list: SnapshotStateList<SMedia> = mutableStateListOf(),
	val isLoading: MutableState<Boolean> = mutableStateOf(false),
)
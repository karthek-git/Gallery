package com.karthek.android.s.gallery.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthek.android.s.gallery.a.MediaFolder
import com.karthek.android.s.gallery.state.db.SMedia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.SoftReference
import javax.inject.Inject

@HiltViewModel
class SMediaViewModel @Inject constructor(private val repo: SMediaRepo) : ViewModel() {
    var sMediaList = MutableLiveData<MutableList<SMedia>?>(null)
    var folderList = MutableLiveData<List<MediaFolder>?>(null)

    fun getFolderContents(index: Int): MutableLiveData<SoftReference<MutableList<SMedia>>?> {
        val folder = folderList.value!![index]
        if (folder.l.value == null) {
            viewModelScope.launch {
                folder.l.value =
                    withContext(Dispatchers.Default) { SoftReference(repo.getSMedia(folder.path)) }
            }
        }
        return folder.l
    }

    init {
        viewModelScope.launch {
            sMediaList.value = repo.getSMedia(null)
            folderList.value = repo.getFolders()
        }
    }
}

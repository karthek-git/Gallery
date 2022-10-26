package com.karthek.android.s.gallery.a

import androidx.lifecycle.MutableLiveData
import com.karthek.android.s.gallery.state.db.SMedia
import java.lang.ref.SoftReference

class MediaFolder(
    var path: String,
    var name: String,
    var previewImage: String,
    val l: MutableLiveData<SoftReference<MutableList<SMedia>>?> = MutableLiveData(null)
)
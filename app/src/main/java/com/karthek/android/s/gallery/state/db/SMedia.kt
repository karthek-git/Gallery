package com.karthek.android.s.gallery.state.db

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class SMedia(
	@PrimaryKey
	var id: Int = 0,

	@ColumnInfo(name = "name")
	var name: String,

	@ColumnInfo(name = "path")
	var path: String,

	@ColumnInfo(name = "lm")
	var date: Long,

	@ColumnInfo(name = "isVideo")
	var isVideo: Boolean = false,

	@ColumnInfo(name = "cat")
	var cat: String?,
) {
	@Ignore
	var uri: Uri? = null

	@Ignore
	var origPos = 0

	@Ignore
	var isHeader = -1

	constructor(
		id: Int, uri: Uri, path: String, name: String, date: Long, isVideo: Boolean, orig_pos: Int,
	) : this(id, name, path, date, isVideo, null) {
		this.uri = uri
		this.origPos = orig_pos
	}

	constructor(
		uri: Uri,
		path: String,
		isVideo: Boolean,
	) : this(0, "", path, 0, isVideo, null) {
		this.uri = uri
	}

	constructor(isHeader: Int, date: Long) : this(0, "_", "_", date, false, null) {
		this.isHeader = isHeader
	}
}
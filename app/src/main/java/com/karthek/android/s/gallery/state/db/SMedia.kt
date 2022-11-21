package com.karthek.android.s.gallery.state.db

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
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

	@ColumnInfo(typeAffinity = ColumnInfo.BLOB)
	var faceEmbeddings: List<FloatArray>? = null,
) {
	@Ignore
	var uri: Uri = makeSMediaUri(id, isVideo)

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
		id: Int, path: String, name: String, date: Long, isVideo: Boolean, orig_pos: Int,
	) : this(id, name, path, date, isVideo, null) {
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

fun makeSMediaUri(id: Int, isVideo: Boolean): Uri {
	val mediaTableUri = if (isVideo) videosUri else imagesUri
	return ContentUris.withAppendedId(mediaTableUri, id.toLong())
}

val imagesUri: Uri = MediaStore.Images.Media.getContentUri("external")
val videosUri: Uri = MediaStore.Video.Media.getContentUri("external")

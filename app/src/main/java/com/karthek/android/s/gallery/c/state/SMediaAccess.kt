package com.karthek.android.s.gallery.c.state

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns
import com.karthek.android.s.gallery.c.a.MFolder
import com.karthek.android.s.gallery.state.db.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SMediaAccess @Inject constructor(
	@ApplicationContext private val context: Context,
	private val sMediaDao: SMediaDao,
) {

	suspend fun getSMedia(
		dir: String = "",
		fromDate: Long = 0,
		sortAsc: Boolean = false,
	): List<SMedia> {
		return withContext(Dispatchers.Default) {
			val list: MutableList<SMedia> = mutableListOf()
			val uri = MediaStore.Files.getContentUri("external")
			val timeColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				FileColumns.DATE_TAKEN
			} else {
				FileColumns.DATE_MODIFIED
			}
			val projection = arrayOf(
				FileColumns._ID,
				FileColumns.DATA,
				FileColumns.DISPLAY_NAME,
				timeColumn,
				FileColumns.MEDIA_TYPE
			)

			var selection = "${FileColumns.MEDIA_TYPE} IN (?, ?)"
			if (dir.isNotEmpty()) selection += " AND ${FileColumns.DATA} LIKE '$dir%'"
			if (fromDate != 0L) selection += " AND ${FileColumns.DATE_MODIFIED} >= $fromDate"
			val sortOrder = if (sortAsc) "$timeColumn ASC" else "$timeColumn DESC"
			val selectionArgs = arrayOf(
				FileColumns.MEDIA_TYPE_IMAGE.toString(),
				FileColumns.MEDIA_TYPE_VIDEO.toString()
			)
			val cursor: Cursor? = context.contentResolver.query(
				uri,
				projection, selection, selectionArgs, sortOrder
			)
			if (cursor != null) {
				val idColumn = cursor.getColumnIndex(FileColumns._ID)
				val pathColumn = cursor.getColumnIndex(FileColumns.DATA)
				val nameColumn = cursor.getColumnIndex(FileColumns.DISPLAY_NAME)
				val dateColumn = cursor.getColumnIndex(timeColumn)
				val typeColumn = cursor.getColumnIndex(FileColumns.MEDIA_TYPE)
				var s = -1
				while (cursor.moveToNext()) {
					val id = cursor.getInt(idColumn)
					s++
					val isVideo = cursor.getInt(typeColumn) == FileColumns.MEDIA_TYPE_VIDEO
					list.add(
						SMedia(
							id, ContentUris.withAppendedId(uri, id.toLong()),
							cursor.getString(pathColumn),
							cursor.getString(nameColumn),
							cursor.getLong(dateColumn),
							isVideo, s
						)
					)
					//Log.v("here", "hey" + cursor.getString(nameColumn));
				}
				cursor.close()
			}
			list
		}
	}

	suspend fun getFolders(): List<MFolder> {
		return withContext(Dispatchers.Default) {
			val uri = MediaStore.Files.getContentUri("external")
			val projection = arrayOf(FileColumns._ID, FileColumns.DATA, FileColumns.MEDIA_TYPE)
			val selection = "${FileColumns.MEDIA_TYPE} IN (?, ?)"
			val selectionArgs = arrayOf(
				FileColumns.MEDIA_TYPE_IMAGE.toString(),
				FileColumns.MEDIA_TYPE_VIDEO.toString()
			)
			val sortOrder = "${FileColumns.DATE_MODIFIED} DESC"

			val cursor =
				context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)

			val foldersMap = mutableMapOf<String, MFolder>()

			if (cursor != null) {
				val pathColumn = cursor.getColumnIndex(FileColumns.DATA)
				while (cursor.moveToNext()) {
					val mediaPath = cursor.getString(pathColumn)
					val parentPath = File(mediaPath).parent!!
					val folder = foldersMap[parentPath] ?: let {
						val idColumn = cursor.getColumnIndex(FileColumns._ID)
						val typeColumn = cursor.getColumnIndex(FileColumns.MEDIA_TYPE)

						val id = cursor.getInt(idColumn)
						val isVideo = (cursor.getInt(typeColumn) == FileColumns.MEDIA_TYPE_VIDEO)
						val previewSMedia =
							SMedia(ContentUris.withAppendedId(uri, id.toLong()), mediaPath, isVideo)
						MFolder(
							path = parentPath,
							name = File(parentPath).name,
							numItems = 0,
							previewSMedia = previewSMedia,
						)
					}
					folder.numItems++
					foldersMap[parentPath] = folder
				}
				cursor.close()
			}
			foldersMap.values.toList()
		}
	}

	suspend fun searchSMedia(query: String): List<SMedia> =
		sMediaDao.findByCat(query)

	suspend fun insertSMedia(sMedia: SMedia) = sMediaDao.insert(sMedia)
	suspend fun insertSFaces(sFace: Array<SFace>) = sMediaDao.insertSFaces(sFace)

	suspend fun getLocalSMedia(): List<SMedia> = sMediaDao.all
	suspend fun getSFaceWithSMedia(): List<SFaceWithSMedia> {
		return withContext(Dispatchers.IO) {
			sMediaDao.getSFaceWithSMedia()
		}
	}

	fun insertSFaceWithSMedia(sFaceSMediaCrossRefs: Array<SFaceSMediaCrossRef>) =
		sMediaDao.insertSFaceWithSMedia(sFaceSMediaCrossRefs)

}
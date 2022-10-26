package com.karthek.android.s.gallery.c.state

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.karthek.android.s.gallery.c.a.MFolder
import com.karthek.android.s.gallery.state.db.SMedia
import com.karthek.android.s.gallery.state.db.SMediaDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Paths
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SMediaAccess @Inject constructor(
	@ApplicationContext private val context: Context,
	private val sMediaDao: SMediaDao
) {

	suspend fun getSMedia(dir: String?): List<SMedia> {
		return withContext(Dispatchers.Default) {
			val list: MutableList<SMedia> = ArrayList()
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

			var selection = FileColumns.MEDIA_TYPE + " IN (?, ?)"
			if (dir != null) selection += " AND " + FileColumns.DATA + " LIKE '" + dir + "%'"
			val sortOrder = "$timeColumn DESC"
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
			val list: MutableList<MFolder> = ArrayList()
			val mediaFolderPaths = ArrayList<String>()
			val uri = MediaStore.Files.getContentUri("external")
			val projection = arrayOf(
				FileColumns.DATA
			)

			val selection = FileColumns.MEDIA_TYPE + " IN (?, ?)"
			val selectionArgs = arrayOf(
				FileColumns.MEDIA_TYPE_IMAGE.toString(),
				FileColumns.MEDIA_TYPE_VIDEO.toString()
			)
			val sortOrder = FileColumns.DATE_MODIFIED + " DESC"

			val cursor: Cursor? = context.contentResolver.query(
				uri,
				projection, selection, selectionArgs, sortOrder
			)
			if (cursor != null) {
				val pathColumn = cursor.getColumnIndex(FileColumns.DATA)
				//int p = cursor.getColumnIndex(FileColumns.PARENT);

				//int p = cursor.getColumnIndex(FileColumns.PARENT);
				while (cursor.moveToNext()) {
					val mediaPath = cursor.getString(pathColumn)
					val parentPath = Paths.get(mediaPath).parent
					//Integer parent = cursor.getInt(p);
					if (!mediaFolderPaths.contains(parentPath.toString())) {
						mediaFolderPaths.add(parentPath.toString())
						val mediaFolder = MFolder(
							parentPath.toString(),
							parentPath.fileName.toString(),
							"file://$mediaPath",
						)
						list.add(mediaFolder)
					}
					//Log.v("here", "hey" + cursor.getString(pathColumn)+cursor.getInt(p));
				}
				cursor.close()
			}
			list
		}
	}

	suspend fun searchSMedia(query: String): List<SMedia> =
		sMediaDao.findByCat(query)

	suspend fun insertSMedia(sMedia: SMedia) = sMediaDao.insert(sMedia)

}

@Singleton
class SMediaAccessPagingSource @Inject constructor(@ApplicationContext private val context: ApplicationContext) :
	PagingSource<String, SMedia>() {
	override suspend fun load(params: LoadParams<String>): LoadResult<String, SMedia> {
		TODO("Not yet implemented")
	}

	override fun getRefreshKey(state: PagingState<String, SMedia>): String? {
		TODO("Not yet implemented")
	}
}
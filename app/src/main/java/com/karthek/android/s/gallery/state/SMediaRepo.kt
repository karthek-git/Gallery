package com.karthek.android.s.gallery.state

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import com.karthek.android.s.gallery.a.MediaFolder
import com.karthek.android.s.gallery.state.db.SMedia
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Paths
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SMediaRepo @Inject constructor(@ApplicationContext private val context: Context) {
    suspend fun getSMedia(dir: String?): MutableList<SMedia> {
        return withContext(Dispatchers.Default) {
            val list: MutableList<SMedia> = ArrayList()
            val uri = MediaStore.Files.getContentUri("external")
            val timeColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.FileColumns.DATE_TAKEN
            } else {
                MediaStore.Files.FileColumns.DATE_MODIFIED
            }
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                timeColumn,
                MediaStore.Files.FileColumns.MEDIA_TYPE
            )

            var selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " IN (?, ?)"
            if (dir != null) selection += " AND " + MediaStore.Files.FileColumns.DATA + " LIKE '" + dir + "%'"
            val sortOrder = "$timeColumn DESC"
            val selectionArgs = arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
            )
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection, selection, selectionArgs, sortOrder
            )
            if (cursor != null) {
                val idColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
                val pathColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                val nameColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dateColumn = cursor.getColumnIndex(timeColumn)
                val typeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)
                var s = -1
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(idColumn)
                    s++
                    val isVideo = cursor.getInt(typeColumn) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
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

    suspend fun getFolders(): List<MediaFolder> {
        return withContext(Dispatchers.Default) {
            val list: MutableList<MediaFolder> = ArrayList()
            val mediaFolderPaths = ArrayList<String>()
            val uri = MediaStore.Files.getContentUri("external")
            val projection = arrayOf(
                MediaStore.Files.FileColumns.DATA
            )

            val selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " IN (?, ?)"
            val selectionArgs = arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
            )
            val sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"

            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection, selection, selectionArgs, sortOrder
            )
            if (cursor != null) {
                val pathColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                //int p = cursor.getColumnIndex(FileColumns.PARENT);

                //int p = cursor.getColumnIndex(FileColumns.PARENT);
                while (cursor.moveToNext()) {
                    val mediaPath = cursor.getString(pathColumn)
                    val parentPath = Paths.get(mediaPath).parent
                    //Integer parent = cursor.getInt(p);
                    if (!mediaFolderPaths.contains(parentPath.toString())) {
                        mediaFolderPaths.add(parentPath.toString())
                        val mediaFolder = MediaFolder(
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

}
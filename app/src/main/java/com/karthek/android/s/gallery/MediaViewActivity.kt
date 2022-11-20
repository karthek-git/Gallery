package com.karthek.android.s.gallery

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import com.karthek.android.s.gallery.c.ui.screens.ExternalMediaViewerScreen
import com.karthek.android.s.gallery.c.ui.theme.AppTheme
import com.karthek.android.s.gallery.state.db.SMedia
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaViewActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
		val uri = intent.data
		if (uri == null) {
			finish()
		}
		setContent { ScreenContent(uri!!) }
	}

	@Composable
	fun ScreenContent(uri: Uri) {
		AppTheme {
			Surface {
				ExternalMediaViewerScreen(
					sMedia = getSMedia(uri),
					onFinish = { finish() })
			}
		}
	}

	private fun getSMedia(uri: Uri): SMedia {
		val sMedia = SMedia(uri, "", false)
		//todo handle uri metadata properly
		val cursor: Cursor =
			contentResolver.query(uri, null, null, null, null) ?: return sMedia
		val nameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
		val sizeIndex: Int = cursor.getColumnIndex(OpenableColumns.SIZE)
		cursor.moveToFirst()
		//viewModel.fileName = cursor.getString(nameIndex)
		//viewModel.origSize = cursor.getLong(sizeIndex)
		cursor.close()
		return sMedia
	}
}
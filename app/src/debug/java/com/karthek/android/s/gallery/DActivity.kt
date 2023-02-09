package com.karthek.android.s.gallery

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.karthek.android.s.gallery.state.ImageInfScreenViewModel
import com.karthek.android.s.gallery.ui.screens.ImageInfScreen
import com.karthek.android.s.gallery.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DActivity : ComponentActivity() {

	private val viewModel: ImageInfScreenViewModel by viewModels()

	private var mGetContent =
		registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
			if (uri == null) return@registerForActivityResult
			viewModel.imageUri = uri
			viewModel.imgBitmap = viewModel.getBitmap(480, 480)
		}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window,false)
		setContent { ScreenContent() }
	}

	@Composable
	fun ScreenContent() {
		AppTheme {
			Surface(modifier = Modifier.fillMaxSize()) {
				ImageInfScreen(viewModel, selectImageClick = {
					mGetContent.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
				})
			}
		}
	}
}




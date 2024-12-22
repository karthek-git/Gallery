package com.karthek.android.s.gallery

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.karthek.android.s.gallery.ui.theme.AppTheme
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
		setContent { ScreenContent() }
	}

	@Composable
	fun ScreenContent() {
		AppTheme {
			Surface {
				val version = remember { getVersionString() }
				SettingsScreen(version)
			}
		}
	}

	private fun getVersionString(): String {
		return "${BuildConfig.VERSION_NAME}-${BuildConfig.BUILD_TYPE} (${BuildConfig.VERSION_CODE})"
	}

	private fun startLicensesActivity() {
		startActivity(Intent(this, LicensesActivity::class.java))
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	fun SettingsScreen(version: String) {
		CommonScaffold(activity = this, name = "About") { paddingValues ->
			Column(modifier = Modifier.padding(paddingValues)) {
				ListItem(
					headlineContent = { Text(text = "Version") },
					supportingContent = { Text(text = version, fontWeight = FontWeight.Light) }
				)
				Divider()
				ListItem(
					headlineContent = { Text(text = "Privacy Policy") },
					modifier = Modifier.clickable {
						val uri =
							Uri.parse("https://policies.karthek.com/Gallery/-/blob/master/privacy.md")
						startActivity(Intent(Intent.ACTION_VIEW, uri))
					})
				Divider()
				ListItem(headlineContent = { Text(text = "Open source licenses") },
					modifier = Modifier.clickable { startLicensesActivity() }
				)
				Divider()
				LicenseBottomSheet {
					val uri = Uri.parse("https://github.com/karthek-git/gic/blob/master/LICENSES/MIT.txt")
					startActivity(Intent(Intent.ACTION_VIEW, uri))
				}
			}
		}
	}

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonScaffold(activity: Activity, name: String, content: @Composable (PaddingValues) -> Unit) {
	val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(text = name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
				navigationIcon = {
					IconButton(onClick = { activity.finish() }) {
						Icon(
							imageVector = Icons.Outlined.ArrowBack,
							contentDescription = stringResource(id = R.string.go_back)
						)
					}
				},
				scrollBehavior = scrollBehavior
			)
		}, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
	) {
		content(it)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseBottomSheet(onClick: () -> Unit) {
	var openBottomSheet by rememberSaveable { mutableStateOf(false) }
	val sheetState = rememberModalBottomSheetState()

	ListItem(headlineContent = { Text(text = "Legal") },
		modifier = Modifier.clickable { openBottomSheet = true }
	)

	if (openBottomSheet) {
		ModalBottomSheetLayout(
			onDismissRequest = { openBottomSheet = false },
			sheetState = sheetState
		) {
			LicenseText(onClick)
		}
	}
}

@Composable
fun LicenseText(onClick: () -> Unit) {
	val annotatedLicenseText = buildAnnotatedString {
		val baseStyle = SpanStyle(color = MaterialTheme.colorScheme.onSurface)
		withStyle(style = baseStyle) {
			append("Copyright © Karthik Alapati\n\n")
			append("This application comes with absolutely no warranty. See the")
		}

		pushStringAnnotation(tag = "lic3", annotation = "link")
		withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
			append(" MIT License ")
		}
		pop()

		withStyle(style = baseStyle) {
			append("for details.")
		}
	}
	ClickableText(
		text = annotatedLicenseText,
		style = MaterialTheme.typography.labelLarge,
		onClick = { onClick() },
		modifier = Modifier
			.padding(16.dp)
			.padding(bottom = 16.dp)
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetLayout(
	onDismissRequest: () -> Unit, sheetState: SheetState, sheetContent: @Composable () -> Unit
) {
	val coroutineScope = rememberCoroutineScope()
	BackHandler(enabled = sheetState.isVisible) {
		coroutineScope.launch { sheetState.hide() }
	}
	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		sheetState = sheetState,
		contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
		content = { sheetContent() }
	)
}
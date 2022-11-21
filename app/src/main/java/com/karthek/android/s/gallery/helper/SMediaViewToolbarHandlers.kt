package com.karthek.android.s.gallery.helper

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.RequiresApi
import androidx.print.PrintHelper
import com.karthek.android.s.gallery.state.db.SMedia


fun shareHandler(context: Context, sMedia: SMedia) {
	val intent = Intent(Intent.ACTION_SEND)
	intent.putExtra(Intent.EXTRA_STREAM, sMedia.uri)
	intent.type = getSMediaType(sMedia)
	intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
	context.startActivity(Intent.createChooser(intent, null))
}

fun editInHandler(context: Context, sMedia: SMedia) {
	val intent = Intent(Intent.ACTION_EDIT)
	intent.setDataAndType(sMedia.uri, getSMediaType(sMedia))
	intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
	context.startActivity(Intent.createChooser(intent, null))
}

fun useAsHandler(context: Context, sMedia: SMedia) {
	val intent = Intent(Intent.ACTION_ATTACH_DATA)
	intent.setDataAndType(sMedia.uri, getSMediaType(sMedia))
	intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
	context.startActivity(Intent.createChooser(intent, null))
}

fun printHandler(context: Context, sMedia: SMedia) {
	val photoPrinter = PrintHelper(context)
	photoPrinter.scaleMode = PrintHelper.SCALE_MODE_FILL
	val bitmap = BitmapFactory.decodeFile(sMedia.path)
	photoPrinter.printBitmap("print", bitmap)
}

fun getSMediaType(sMedia: SMedia): String {
	return if (sMedia.isVideo) "video/" else "image/*"
}

fun trashHandler(context: Context, sMedia: SMedia) {
	context.contentResolver.delete(sMedia.uri, null, null)
}

@RequiresApi(Build.VERSION_CODES.R)
fun trashHandler(
	context: Context,
	sMedia: SMedia,
	trashLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
) {
	Log.v("trash:","${sMedia.uri}")
	val pendingIntent =
		MediaStore.createTrashRequest(context.contentResolver, listOf(sMedia.uri), true)
	trashLauncher.launch(IntentSenderRequest.Builder(pendingIntent).build())
}
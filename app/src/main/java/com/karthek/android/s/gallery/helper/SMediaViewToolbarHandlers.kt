package com.karthek.android.s.gallery.helper

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.print.PrintHelper
import com.karthek.android.s.gallery.state.db.SMedia


fun shareHandler(context: Context, sMedia: SMedia) {
	val intent = Intent(Intent.ACTION_SEND)
	intent.putExtra(Intent.EXTRA_STREAM, sMedia.uri)
	intent.type = "image/*"
	intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
	context.startActivity(Intent.createChooser(intent, null))
}

fun editInHandler(context: Context,sMedia: SMedia){
	val intent=Intent(Intent.ACTION_EDIT)
	intent.setDataAndType(sMedia.uri, "image/*")
	intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
	context.startActivity(Intent.createChooser(intent, null))
}

fun useAsHandler(context: Context, sMedia: SMedia) {
	val intent = Intent(Intent.ACTION_ATTACH_DATA)
	intent.setDataAndType(sMedia.uri, "image/*")
	intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
	context.startActivity(Intent.createChooser(intent, null))
}

fun printHandler(context: Context, sMedia: SMedia) {
	val photoPrinter = PrintHelper(context)
	photoPrinter.scaleMode = PrintHelper.SCALE_MODE_FILL
	val bitmap = BitmapFactory.decodeFile(sMedia.path)
	photoPrinter.printBitmap("print", bitmap)
}
package com.karthek.android.s.gallery.state.db

import androidx.room.TypeConverter
import com.karthek.android.s.gallery.model.N_EMBEDDING_DIMENSIONS
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

class Converters {

	@TypeConverter
	fun byteArrayFromFloatArrays(floats: List<FloatArray>): ByteArray {
		val bas = ByteArrayOutputStream()
		val ds = DataOutputStream(bas)
		floats.forEach { floatArray -> floatArray.forEach { float -> ds.writeFloat(float) } }
		return bas.toByteArray()
	}

	@TypeConverter
	fun floatArraysFromByteArrays(bytes: ByteArray): List<FloatArray> {
		val bas = ByteArrayInputStream(bytes)
		val ds = DataInputStream(bas)
		val n = ((bytes.size) / (4 * N_EMBEDDING_DIMENSIONS))
		val floats = List(n) { FloatArray(N_EMBEDDING_DIMENSIONS) }
		floats.forEach { floatArray ->
			for (i in floatArray.indices) {
				floatArray[i] = ds.readFloat()
			}
		}
		return floats
	}

}
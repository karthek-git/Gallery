package com.karthek.android.s.gallery.state.db

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class ConvertersTest {

	@Test
	fun byteArrayFromFloatArrays() {
	}

	@Test
	fun floatArraysFromByteArrays() {
		val converters = Converters()
		val byteArrays = converters.byteArrayFromFloatArrays(listOf(floatArrayOf(0f, 1f, 3f)))
		assertArrayEquals(floatArrayOf(0f, 1f, 3f),	converters.floatArraysFromByteArrays(byteArrays)?.get(0),0f)
	}
}
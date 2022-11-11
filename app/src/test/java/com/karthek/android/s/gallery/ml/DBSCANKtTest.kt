package com.karthek.android.s.gallery.ml

import org.junit.Assert.assertEquals
import org.junit.Test

class DBSCANKtTest {

	@Test
	fun l2Norm() {
		assertEquals(3.7416575f, l2Norm(floatArrayOf(1f, 2f, 3f)))
	}

	@Test
	fun dbscan() {
		assertEquals(1, dbscan(listOf(
			Pair(floatArrayOf(0.5f, 0.5f), 0),
			Pair(floatArrayOf(0.7f, 0.7f), 0),
			Pair(floatArrayOf(0.6f, 0.6f), 0),
			Pair(floatArrayOf(0.45f, 0.45f), 0),
			Pair(floatArrayOf(0.55f, 0.55f), 0),
			Pair(floatArrayOf(0.65f, 0.6f), 0)
		)).second)
		assertEquals(2, dbscan(listOf(
			Pair(floatArrayOf(0.5f, 0.5f), 0),
			Pair(floatArrayOf(0.7f, 0.7f), 0),
			Pair(floatArrayOf(0.6f, 0.6f), 0),
			Pair(floatArrayOf(0.45f, 0.45f), 0),
			Pair(floatArrayOf(0.55f, 0.55f), 0),
			Pair(floatArrayOf(0.5f, 0.5f), 0),
			Pair(floatArrayOf(1.7f, 1.7f), 0),
			Pair(floatArrayOf(1.6f, 1.6f), 0),
			Pair(floatArrayOf(1.45f, 1.45f), 0),
			Pair(floatArrayOf(1.55f, 1.55f), 0),
			Pair(floatArrayOf(1.65f, 1.6f), 0),
		)).second)
	}
}
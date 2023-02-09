package com.karthek.android.s.gallery.ml

import org.junit.Assert.assertEquals
import org.junit.Test

class DBSCANKtTest {

	@Test
	fun l2Norm() {
		assertEquals(3.7416575f, l2Norm(floatArrayOf(1f, 2f, 3f)))
	}

	@Test
	fun vDist() {
		val p = floatArrayOf(1.0f, 2.0f, 3.0f)
		val i = floatArrayOf(9.0f, 8.0f, 7.0f)
		assertEquals(
			l2Norm(FloatArray(p.size) { index -> i[index] - p[index] }),
			l2Norm(FloatArray(i.size) { index -> p[index] - i[index] })
		)
	}

	@Test
	fun dbscan() {
		assertEquals(
			1, dbscan(
				listOf(
					Pair(floatArrayOf(0.5f, 0.5f), 0),
					Pair(floatArrayOf(0.7f, 0.7f), 0),
					Pair(floatArrayOf(0.6f, 0.6f), 0),
					Pair(floatArrayOf(0.45f, 0.45f), 0),
					Pair(floatArrayOf(0.55f, 0.55f), 0),
					Pair(floatArrayOf(0.65f, 0.6f), 0)
				)
			).second
		)
		assertEquals(
			2, dbscan(
				listOf(
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
				)
			).second
		)
	}
}
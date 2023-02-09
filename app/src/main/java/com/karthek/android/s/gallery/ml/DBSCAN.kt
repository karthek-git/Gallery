package com.karthek.android.s.gallery.ml

import com.karthek.android.s.gallery.state.db.SFaceSMediaCrossRef
import kotlin.math.sqrt

fun l2Norm(x: FloatArray): Float {
	return sqrt(x.fold(0f) { acc, fl -> (acc + (fl * fl)) })
}

fun dbscan(
	x: List<Pair<FloatArray, Int>>,
	eps: Float = 0.5f,
	minInstances: Int = 5,
): Pair<Array<SFaceSMediaCrossRef>, Int> {
	val xNeighbours = mutableListOf<Pair<Boolean, List<Int>>>()

	x.forEach { p ->
		val neighbours = mutableListOf<Int>()
		x.forEachIndexed { iIndex, i ->
			val l2 = l2Norm(FloatArray(i.first.size) { index -> p.first[index] - i.first[index] })
			if ((l2 > 0) && (l2 <= eps)) {
				neighbours.add(iIndex)
			}
		}
		val isCore = (neighbours.size >= (minInstances - 1))
		xNeighbours.add(Pair(isCore, neighbours))
	}

	val xLabels = Array(x.size) { SFaceSMediaCrossRef(-1, -1) }

	var label = -1
	for (pIndex in x.indices) {
		if ((xLabels[pIndex].faceId == -1) && (xNeighbours[pIndex].first)) {
			val nCp = ArrayDeque<Int>()
			label++
			nCp.add(pIndex)
			while (nCp.isNotEmpty()) {
				val tempCp = nCp.removeLast()
				xLabels[tempCp].faceId = label
				xLabels[tempCp].SMediaId = x[tempCp].second
				xNeighbours[tempCp].second.forEach { nP -> if (xLabels[nP].faceId == -1) nCp.add(nP) }
			}
		}
	}

	return Pair(xLabels, label + 1)
}
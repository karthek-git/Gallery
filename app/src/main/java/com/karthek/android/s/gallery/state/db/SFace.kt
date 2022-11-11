package com.karthek.android.s.gallery.state.db

import androidx.room.*

@Entity
data class SFace(
	@PrimaryKey
	val id: Int,
	val name: String,
)

@Entity(primaryKeys = ["faceId", "SMediaId"])
data class SFaceSMediaCrossRef(
	var faceId: Int,
	val SMediaId: Int,
)

data class SFaceWithSMedia(
	@Embedded val sFace: SFace,
	@Relation(
		parentColumn = "id",
		entity = SMedia::class,
		entityColumn = "id",
		associateBy = Junction(
			value = SFaceSMediaCrossRef::class,
			parentColumn = "faceId",
			entityColumn = "SMediaId")
	)
	val SMediaList: List<SMedia>,
)

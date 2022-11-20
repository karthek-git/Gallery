package com.karthek.android.s.gallery.state.db

import androidx.room.*

@Entity
data class SCategory(
	@PrimaryKey
	val id: Int,
	val name: String,
)

@Entity(primaryKeys = ["categoryId", "SMediaId"])
data class SCategorySMediaCrossRef(
	val categoryId: Int,
	val SMediaId: Int,
)

data class SCategoryWithSMedia(
	@Embedded val sCategory: SCategory,
	@Relation(
		parentColumn = "id",
		entity = SMedia::class,
		entityColumn = "id",
		associateBy = Junction(
			value = SCategorySMediaCrossRef::class,
			parentColumn = "categoryId",
			entityColumn = "SMediaId"
		)
	)
	val SMediaList: List<SMedia>,
)
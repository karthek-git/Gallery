package com.karthek.android.s.gallery.state.db

import androidx.room.*

@Dao
interface SMediaDao {
	@get:Query("SELECT * FROM smedia")
	val all: List<SMedia>

	@Query("SELECT * FROM smedia WHERE id IN (:ids)")
	fun loadAllByIds(ids: IntArray): List<SMedia>

	@Query("SELECT * FROM smedia WHERE name LIKE :name LIMIT 1")
	fun findByName(name: String): SMedia

	@Query("SELECT * FROM smedia WHERE cat LIKE '%' || :cat || '%'")
	suspend fun findByCat(cat: String): List<SMedia>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(sMedia: SMedia)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertSFaces(sFaces: Array<SFace>)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertSCategories(sCategories: List<SCategory>)

	@Insert
	fun insertAll(sMedia: List<SMedia>)

	@Update
	suspend fun update(sMedia: SMedia)

	@Delete
	fun delete(sMedia: SMedia)

	@Transaction
	@Query("SELECT * FROM SFace")
	suspend fun getSFacesWithSMedia(): List<SFaceWithSMedia>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertSFacesWithSMedia(sFaceSMediaCrossRefs: Array<SFaceSMediaCrossRef>)

	@Transaction
	@Query("SELECT * FROM SCategory")
	suspend fun getSCategoriesWithSMedia(): List<SCategoryWithSMedia>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertSCategoryWithSMedia(sCategorySMediaCrossRef: SCategorySMediaCrossRef)
}
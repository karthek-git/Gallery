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
	suspend fun insertSFaces(sFace: Array<SFace>)

	@Insert
	fun insertAll(sMedia: List<SMedia>)

	@Update
	suspend fun update(sMedia: SMedia)

	@Delete
	fun delete(sMedia: SMedia)

	@Transaction
	@Query("SELECT * FROM SFace")
	fun getSFaceWithSMedia(): List<SFaceWithSMedia>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insertSFaceWithSMedia(sFaceSMediaCrossRefs: Array<SFaceSMediaCrossRef>)
}
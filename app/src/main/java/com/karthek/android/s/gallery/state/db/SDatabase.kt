package com.karthek.android.s.gallery.state.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SMedia::class, SFace::class, SFaceSMediaCrossRef::class], version = 1)
@TypeConverters(Converters::class)
abstract class SDatabase : RoomDatabase() {
	abstract fun sMediaDao(): SMediaDao
}
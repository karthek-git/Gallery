package com.karthek.android.s.gallery.state.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SMedia::class], version = 1)
abstract class SDatabase : RoomDatabase() {
    abstract fun sMediaDao(): SMediaDao
}
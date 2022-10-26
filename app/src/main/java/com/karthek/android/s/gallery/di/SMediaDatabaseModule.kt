package com.karthek.android.s.gallery.di

import android.content.Context
import androidx.room.Room
import com.karthek.android.s.gallery.state.db.SDatabase
import com.karthek.android.s.gallery.state.db.SMediaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object SMediaDatabaseModule {
	@Provides
	@Singleton
	fun provideSMediaDatabase(@ApplicationContext context: Context): SDatabase =
		Room.databaseBuilder(
			context.applicationContext,
			SDatabase::class.java,
			"SDatabase"
		).build()

	@Provides
	fun provideAppDao(database: SDatabase): SMediaDao = database.sMediaDao()
}
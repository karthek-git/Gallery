package com.karthek.android.s.gallery.c.state

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "prefs")

val CLASSIFY_CAT_KEY = booleanPreferencesKey("classify_cat")
val LAST_DATE_KEY = longPreferencesKey("last_date")
val LAST_MS_VERSION_KEY = longPreferencesKey("last_ms_ver")
val LAST_MS_GEN_KEY = longPreferencesKey("last_ms_gen")
val LAST_ML_VERSION_KEY = intPreferencesKey("last_ml_ver")

data class UserPrefs(
	val showHidden: Boolean,
	val lastDate: Long,
	val lastMSVersion: Long,
	val lastMSGeneration: Long,
	val lastMLVersion: Int,
)

@Singleton
class Prefs @Inject constructor(@ApplicationContext context: Context) {

	private val dataStore = context.dataStore

	val prefsFlow = dataStore.data.map { prefs ->
		val classifyCat = prefs[CLASSIFY_CAT_KEY] ?: false
		val lastDate = prefs[LAST_DATE_KEY] ?: 0
		val lastMSVersion = prefs[LAST_MS_VERSION_KEY] ?: 0
		val lastMSGeneration = prefs[LAST_MS_GEN_KEY] ?: 0
		val lastMLVersion = prefs[LAST_ML_VERSION_KEY] ?: 0
		UserPrefs(classifyCat, lastDate, lastMSVersion, lastMSGeneration, lastMLVersion)
	}

	suspend fun onClassifyCatChange(classifyCat: Boolean) {
		dataStore.edit { mutablePreferences ->
			mutablePreferences[CLASSIFY_CAT_KEY] = classifyCat
		}
	}

	suspend fun onLastDateChange(date: Long) {
		dataStore.edit { mutablePreferences ->
			mutablePreferences[LAST_DATE_KEY] = date
		}
	}

	suspend fun onLastMSVersionChange(version: Long) {
		dataStore.edit { mutablePreferences ->
			mutablePreferences[LAST_MS_VERSION_KEY] = version
		}
	}

	suspend fun onLastMSGenerationChange(gen: Long) {
		dataStore.edit { mutablePreferences ->
			mutablePreferences[LAST_MS_GEN_KEY] = gen
		}
	}

	suspend fun onLastMLVersionChange(version: Int) {
		dataStore.edit { mutablePreferences ->
			mutablePreferences[LAST_ML_VERSION_KEY] = version
		}
	}
}
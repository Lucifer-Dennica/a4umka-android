package com.example.a4umka.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val SHOW_LABELS = booleanPreferencesKey("show_labels")
        val FAVORITES = stringSetPreferencesKey("favorites")
    }

    val showLabels: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SHOW_LABELS] ?: true
    }

    val favorites: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.FAVORITES] ?: emptySet()
    }

    suspend fun setShowLabels(value: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_LABELS] = value }
    }

    suspend fun toggleFavorite(taskId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES] ?: emptySet()
            prefs[Keys.FAVORITES] = if (taskId in current) current - taskId else current + taskId
        }
    }
}

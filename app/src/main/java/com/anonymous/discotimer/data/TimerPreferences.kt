package com.anonymous.discotimer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "timer_preferences")

class TimerPreferences(private val context: Context) {

    companion object {
        private val WORK_KEY = intPreferencesKey("work")
        private val CYCLES_KEY = intPreferencesKey("cycles")
        private val SETS_KEY = intPreferencesKey("sets")
        private val IS_MUTED_KEY = booleanPreferencesKey("isMuted")
    }

    val work: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[WORK_KEY] ?: 40
    }

    val cycles: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[CYCLES_KEY] ?: 3
    }

    val sets: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SETS_KEY] ?: 2
    }

    val isMuted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_MUTED_KEY] ?: false
    }

    suspend fun setWork(work: Int) {
        context.dataStore.edit { preferences ->
            preferences[WORK_KEY] = work
        }
    }

    suspend fun setCycles(cycles: Int) {
        context.dataStore.edit { preferences ->
            preferences[CYCLES_KEY] = cycles
        }
    }

    suspend fun setSets(sets: Int) {
        context.dataStore.edit { preferences ->
            preferences[SETS_KEY] = sets
        }
    }

    suspend fun setMuted(muted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_MUTED_KEY] = muted
        }
    }
}

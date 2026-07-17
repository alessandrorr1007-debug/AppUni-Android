package com.example.agendapx.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

object UserPreferences {

    private val KEY_USER_ID = stringPreferencesKey("user_id")
    private val KEY_USER_NAME = stringPreferencesKey("user_name")
    private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val KEY_REMEMBER_SESSION = booleanPreferencesKey("remember_session")

    val userId: Flow<String>
        get() = context.dataStore.data.map { prefs -> prefs[KEY_USER_ID] ?: "" }

    val userName: Flow<String>
        get() = context.dataStore.data.map { prefs -> prefs[KEY_USER_NAME] ?: "" }

    val isLoggedIn: Flow<Boolean>
        get() = context.dataStore.data.map { prefs -> prefs[KEY_IS_LOGGED_IN] ?: false }

    val rememberSession: Flow<Boolean>
        get() = context.dataStore.data.map { prefs -> prefs[KEY_REMEMBER_SESSION] ?: false }

    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    suspend fun saveSession(userId: String, userName: String, remember: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_NAME] = userName
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_REMEMBER_SESSION] = remember
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = ""
            prefs[KEY_USER_NAME] = ""
            prefs[KEY_IS_LOGGED_IN] = false
            prefs[KEY_REMEMBER_SESSION] = false
        }
    }

    suspend fun getCurrentUserId(): String {
        return context.dataStore.data.map { prefs ->
            prefs[KEY_USER_ID] ?: ""
        }.first()
    }

    suspend fun isRememberSessionEnabled(): Boolean {
        return context.dataStore.data.map { prefs ->
            prefs[KEY_REMEMBER_SESSION] ?: false
        }.first()
    }
}

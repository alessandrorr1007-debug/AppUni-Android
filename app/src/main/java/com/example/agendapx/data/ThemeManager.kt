package com.example.agendapx.data

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.flow.Flow

object ThemeManager {

    const val MODE_LIGHT = "light"
    const val MODE_DARK = "dark"
    const val MODE_SYSTEM = "system"

    fun setTheme(mode: String) {
        when (mode) {
            MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            MODE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            MODE_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun applyCurrentTheme(activity: Activity) {
        val mode = kotlinx.coroutines.runBlocking { UserPreferences.getThemeMode() }
        setTheme(mode)
    }

    fun getCurrentMode(): Int {
        return AppCompatDelegate.getDefaultNightMode()
    }

    fun getThemeModeFlow(): Flow<String> {
        return UserPreferences.getThemeModeFlow()
    }
}

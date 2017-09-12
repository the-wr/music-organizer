package com.wr.musicorganizer

import android.app.Application
import android.content.ContextWrapper
import com.pixplicity.easyprefs.library.Prefs


class App : Application() {
    companion object {
        const val PREF_KEY_VOTE_COUNT = "PREF_KEY_VOTE_COUNT"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize the Prefs class
        Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(packageName)
                .setUseDefaultSharedPreference(true)
                .build()

        // Fill some default values
        if (Prefs.getInt(PREF_KEY_VOTE_COUNT, -1) == -1)
            Prefs.putInt(PREF_KEY_VOTE_COUNT, 2)
    }
}
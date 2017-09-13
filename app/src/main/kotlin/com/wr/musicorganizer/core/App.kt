package com.wr.musicorganizer.core

import android.app.Application
import android.content.ContextWrapper
import com.pixplicity.easyprefs.library.Prefs
import com.wr.musicorganizer.utils.doInBackground
import io.reactivex.Completable


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

        val c = Completable.create { println("--- c"); it.onComplete() }
        val c2 = c.andThen( Completable.create { println("--- c2"); it.onComplete() })
        val c3 = c2.doInBackground().doAfterTerminate { println("--- afterTerminate") }

        c3.subscribe()
    }
}
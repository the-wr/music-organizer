package com.wr.musicorganizer.core

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.Completable
import io.reactivex.Single
import java.io.InputStreamReader


data class Song(val fileName: String, var score: Int,
                val artist: String?, val title: String?, val album: String?, val trackNumber: Int?)

data class SongsWrapper(val songs: List<Song>)

class Library {
    companion object {
        const val LIBRARY_FILE_NAME = "library.json"

        fun load(context: Context) = Single.create<Library> {
            try {
                val stream = context.openFileInput(LIBRARY_FILE_NAME)
                val songsWrapper = Gson().fromJson(InputStreamReader(stream), SongsWrapper::class.java)

                it.onSuccess(Library().also { it.songs = songsWrapper.songs.toMutableList() })
            } catch (e: Throwable) {
                it.onError(e)
            }
        }

        fun save(context: Context, library: Library) = Completable.create {
            try {
                val gson = GsonBuilder().setPrettyPrinting().create()
                val json = gson.toJson(SongsWrapper(library.songs))

                val stream = context.openFileOutput(LIBRARY_FILE_NAME, Context.MODE_PRIVATE)
                stream.write(json.toByteArray())
                stream.close()

                it.onComplete()
            } catch (e: Throwable) {
                it.onError(e)
            }
        }
    }

    var songs = mutableListOf<Song>()
}
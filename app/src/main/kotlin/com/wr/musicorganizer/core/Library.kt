package com.wr.musicorganizer.core

import android.content.Context
import com.google.gson.Gson
import com.wr.musicorganizer.utils.FileUtils
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.io.InputStreamReader


data class Track(val fileName: String, var score: Int,
                 val artist: String?, val title: String?, val album: String?, val trackNumber: Int?)

data class SongsWrapper(val tracks: List<Track>)

class Library {
    companion object {
        const val LIBRARY_FILE_NAME = "library.json"

        fun load(context: Context) = Single.create<Library> {
            try {
                val stream = context.openFileInput(LIBRARY_FILE_NAME)
                val songsWrapper = Gson().fromJson(InputStreamReader(stream), SongsWrapper::class.java)
                stream.close()

                it.onSuccess(Library().also { it.songs = songsWrapper.tracks.toMutableList() })
            } catch (e: Throwable) {
                it.onError(e)
            }
        }

        fun save(context: Context, library: Library) =
                FileUtils.save(context, SongsWrapper(library.songs), LIBRARY_FILE_NAME)
                        /*
                        .andThen {
                            //Completable.create {
                                try {
                                    FileUtils.copy(File(context.filesDir, LIBRARY_FILE_NAME), File("/storage/0000-0000/library.json"))
                                } catch (e: Throwable) {
                                    val a = 1
                                }
                                it.onComplete()
                            //}
                        }*/
    }

    var songs = mutableListOf<Track>()
}
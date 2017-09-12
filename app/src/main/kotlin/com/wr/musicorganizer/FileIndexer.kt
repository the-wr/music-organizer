package com.wr.musicorganizer

import android.content.Context
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import java.io.File


object FileIndexer {
    fun indexFiles(context: Context, folder: String): Observable<String> = Observable.create {
        try {
            process(context, File(folder), it)
        } catch (e: Throwable) {
            it.onError(e)
        }
        it.onComplete()
    }

    private fun process(context: Context, folder: File, emitter: ObservableEmitter<String>) {
        folder.listFiles().forEach {
            if (it.isDirectory) process(context, it, emitter)
            else emitter.onNext(it.absolutePath)
        }
    }
}
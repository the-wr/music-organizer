package com.wr.musicorganizer.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import com.wr.musicorganizer.core.Song
import io.reactivex.Observable


object Indexer {
    fun reindexMusic(context: Context, folder: String): Observable<Song> =
        FileIndexer.indexFiles(context, folder)
                .filter { it.endsWith(".mp3") }
                .map {
                    try {
                        val mmr = MediaMetadataRetriever()
                        mmr.setDataSource(it)

                        val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                        val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                        val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                        val trackNumberStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)

                        mmr.release()

                        var trackNumber = -1;
                        try {
                            trackNumber = trackNumberStr.toInt()
                        } catch (t: Throwable) {
                        }

                        Song(it, 0, artist, title, album, if (trackNumber >= 0) trackNumber else null)
                    } catch (t: Throwable) {
                        Song(it, 0, null, null, null, null)
                    } finally {
                    }
                }
                /*
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            //val list = it
                            //Toast.makeText(this, "${list.size}", Toast.LENGTH_LONG).show()

                            //val gson = GsonBuilder().setPrettyPrinting().create()
                            //val json = gson.toJson(list)

                            //val public = Environment.getExternalStorageDirectory()
                            //val file = File(public, "files.json")
                            //val s = FileOutputStream(file)
                            //s.write(json.toByteArray())
                            //s.close()
                        },
                        { })*/
    //}
}
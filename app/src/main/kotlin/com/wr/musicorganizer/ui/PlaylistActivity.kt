package com.wr.musicorganizer.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.pixplicity.easyprefs.library.Prefs
import com.wr.musicorganizer.core.App
import com.wr.musicorganizer.R
import com.wr.musicorganizer.core.Track
import kotlinx.android.synthetic.main.activity_playlist.*
import java.util.*

class PlaylistActivity : PlayerServiceAttachedActivity() {

    companion object {
        fun intent(context: Context) = Intent(context, PlaylistActivity::class.java)
        val random = Random(UUID.randomUUID().hashCode().toLong())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)

        val threshold = Prefs.getInt(App.PREF_KEY_VOTE_COUNT, 0)

        button_play_lib_all_random.setOnClickListener {
            shuffleAndSetPlaylist(service!!.library.songs.filter { it.score >= threshold })
        }
        button_play_unsorted_all_random.setOnClickListener {
            shuffleAndSetPlaylist(service!!.library.songs.filter { it.score < threshold && it.score > -threshold })
        }
    }

    private fun shuffleAndSetPlaylist(tracks: Collection<Track>) {
        val shuffleList = mutableListOf<Track>()
        shuffleList.addAll(tracks)

        val size = shuffleList.size
        for (i in shuffleList.indices) {
            val k = random.nextInt(size)

            val tmp = shuffleList[i]
            shuffleList[i] = shuffleList[k]
            shuffleList[k] = tmp
        }

        service!!.setPlaylist(shuffleList)
        service!!.play()

        startActivity(MainActivity.intent(this))
        finish()
    }
}

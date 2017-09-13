package com.wr.musicorganizer.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.wr.musicorganizer.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : PlayerServiceAttachedActivity() {

    companion object {
        fun intent(context: Context) = Intent(context, MainActivity::class.java)
    }

    var progressSubscription: Disposable? = null
    var detailsSubscription: Disposable? = null
    var reindexSubscription: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_play_stop.setOnClickListener {
            if(service!!.isPlaying()) {
                service!!.stop()
            } else {
                service!!.play()
            }
        }
        button_prev.setOnClickListener { service!!.prevTrack() }
        button_next.setOnClickListener { service!!.nextTrack() }
        button_playlist.setOnClickListener { startActivity(PlaylistActivity.intent(this)) }

        button_keep.setOnClickListener { service!!.keepCurrentTrack() }
        button_discard.setOnClickListener { service!!.discardCurrentTrack() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_item_check_permissions -> isStoragePermissionGranted()
            R.id.menu_item_reindex -> service?.reindex("/storage/0000-0000/!My/Music")
            R.id.menu_item_add_all -> if (service != null && service!!.library != null) service!!.setPlaylist(service!!.library!!.songs)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        super.onServiceConnected(name, binder)

        progressSubscription = service!!.progress.subscribe {
            seekbar_progress.max = it.secondsTotal
            seekbar_progress.progress = it.secondsPlayed

            textview_progress_current.text = it.secondsPlayed.toString()
            textview_progress_total.text = it.secondsTotal.toString()

            if (service!!.isPlaying()) {
                button_play_stop.text = "Stop"
            } else {
                button_play_stop.text = "Play"
            }
        }

        detailsSubscription = service!!.trackDetails.subscribe {
            textview_track_artist.text = if (it.track.artist.isNullOrEmpty()) "" else it.track.artist
            textview_track_name.text = if (it.track.artist.isNullOrEmpty()) it.track.fileName else it.track.title
            textview_track_count.text = "${it.trackNumber + 1} / ${it.trackCount}"
        }

        reindexSubscription = service!!.reindexObservable
                .throttleLast(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Toast.makeText(this, "Reindex: $it", Toast.LENGTH_SHORT).show()
                }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        progressSubscription?.dispose()
        detailsSubscription?.dispose()
        reindexSubscription?.dispose()

        super.onServiceDisconnected(name)
    }

    fun isStoragePermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true
            } else {
                ActivityCompat.requestPermissions(this, arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                return false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("", "Permission: " + permissions[0] + "was " + grantResults[0])
            //resume tasks needing this permission
        }
    }
}

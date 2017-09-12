package com.wr.musicorganizer.core

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import com.wr.musicorganizer.*
import com.wr.musicorganizer.ui.MainActivity
import com.wr.musicorganizer.utils.Indexer
import com.wr.musicorganizer.utils.doInBackground
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit


data class PlaybackProgress(val secondsPlayed: Int, val secondsTotal: Int)
data class TrackDetails(val track: Song, val trackNumber: Int, val trackCount: Int)

interface IPlayerService {
    fun reindex(folder: String)
    fun setPlaylist(songs: List<Song>)

    fun play()
    fun stop()
    //fun togglePause()
    fun isPlaying(): Boolean

    fun nextTrack()
    fun prevTrack()

    val progress: Observable<PlaybackProgress>
    val trackDetails: Observable<TrackDetails>
    val reindexObservable: Observable<Int>

    val library: Library
}

class MainService : Service(), IPlayerService {
    interface IMainServiceBinder : IBinder {
        val playerService: IPlayerService
    }

    companion object {
        const val NOTIFICATION_ID = 1
        fun intent(context:Context) = Intent(context, MainService::class.java)
    }

    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private val timer = Observable.interval(500, TimeUnit.MILLISECONDS)
    private var shutdownSubscription: Disposable? = null

    private var playlist: List<Song>? = null
    private var currentTrackIndex: Int = 0

    override var library: Library = Library()
    override val progress: BehaviorSubject<PlaybackProgress> = BehaviorSubject.create()
    override val trackDetails: BehaviorSubject<TrackDetails> = BehaviorSubject.create()
    override val reindexObservable: PublishSubject<Int> = PublishSubject.create<Int>()

    override fun onBind(intent: Intent): IBinder? {
        shutdownSubscription?.dispose()

        return object : Binder(), IMainServiceBinder {
            override val playerService: IPlayerService
                get() = this@MainService
        }
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)

        shutdownSubscription?.dispose()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        shutdownSubscription = Observable.timer(1000, TimeUnit.MILLISECONDS)
                .subscribe {
                    if (!mediaPlayer.isPlaying) {
                        stopSelf()
                    }
                }

        return true
    }

    override fun onCreate() {
        super.onCreate()

        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)
        val notification = Notification.Builder(applicationContext)
                .setContentTitle("title")
                .setContentText("text")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(NOTIFICATION_ID, notification)

        mediaPlayer.setOnPreparedListener { mediaPlayer.start() }
        mediaPlayer.setOnCompletionListener { nextTrack() }

        timer.observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    progress.onNext(PlaybackProgress(mediaPlayer.currentPosition / 1000, mediaPlayer.duration / 1000))
                }

        Library.load(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { library = it },
                        { Toast.makeText(this, "Error loading library!", Toast.LENGTH_LONG).show() }
                )
    }

    override fun onDestroy() {
        super.onDestroy()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    // -----

    override fun reindex(folder: String) {
        var count = 0
        Indexer.reindexMusic(this, folder)
                .doInBackground()
                .doOnNext {
                    count++
                    reindexObservable.onNext(count)
                }
                .toList()
                .subscribe({
                    Toast.makeText(this, "Reindex done: ${it.count()}", Toast.LENGTH_LONG).show()

                    library = Library().apply { songs = it }
                    Library.save(this, library)
                            .doInBackground()
                            .subscribe({
                                Toast.makeText(this, "Save success", Toast.LENGTH_LONG).show()
                            }, {
                                Toast.makeText(this, "Save error!", Toast.LENGTH_LONG).show()
                            })
                }, {
                    Toast.makeText(this, "Reindex error!", Toast.LENGTH_LONG).show()
                })
    }

    override fun setPlaylist(songs: List<Song>) {
        playlist = songs
        currentTrackIndex = 0
    }

    override fun play() {
        if (playlist == null || currentTrackIndex >= playlist!!.size) {
            trackDetails.onNext(TrackDetails(Song("", 0, null, null, null, null), 0, 0))
            return
        }

        val track = playlist!![currentTrackIndex]

        mediaPlayer.reset()
        mediaPlayer.setDataSource(track.fileName)
        mediaPlayer.prepareAsync()

        trackDetails.onNext(TrackDetails(track, currentTrackIndex, playlist!!.size))
    }

    override fun stop() {
        mediaPlayer.stop()
    }

    override fun isPlaying() = mediaPlayer.isPlaying

    fun togglePause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.start()
        }
    }

    override fun nextTrack() {
        stop()
        if (playlist != null && currentTrackIndex < playlist!!.size - 1) {
            currentTrackIndex++
            play()
        }
    }

    override fun prevTrack() {
        stop()
        if (playlist != null && currentTrackIndex > 0) {
            currentTrackIndex--
            play()
        }
    }

    // -----
}
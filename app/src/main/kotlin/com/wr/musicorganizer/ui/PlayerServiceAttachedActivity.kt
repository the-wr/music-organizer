package com.wr.musicorganizer.ui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import com.wr.musicorganizer.core.IPlayerService
import com.wr.musicorganizer.core.MainService


@SuppressLint("Registered")
open class PlayerServiceAttachedActivity : AppCompatActivity(), ServiceConnection {

    var service: IPlayerService? = null

    override fun onResume() {
        super.onResume()

        if (service == null) {
            val intent = MainService.intent(this)
            startService(intent)
            bindService(intent, this, 0)
        }
    }

    override fun onPause() {
        if (service != null) {
            unbindService(this)
            onServiceDisconnected(null)
        }

        super.onPause()
    }

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        service = (binder as MainService.IMainServiceBinder).playerService
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
    }
}
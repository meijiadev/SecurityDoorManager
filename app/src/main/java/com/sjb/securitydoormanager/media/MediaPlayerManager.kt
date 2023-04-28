package com.sjb.securitydoormanager.media

import android.content.Context
import android.media.MediaPlayer

object MediaPlayerManager {

    private var player: MediaPlayer? = null

    fun setDataSource(context: Context, urlRes: Int) {
        player = MediaPlayer.create(context, urlRes)
        player?.start()
    }



}
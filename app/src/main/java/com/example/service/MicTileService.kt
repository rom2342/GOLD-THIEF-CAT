package com.example.service

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class MicTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentlyMuted = audioManager.isMicrophoneMute
        audioManager.isMicrophoneMute = !currentlyMuted
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val isMuted = audioManager.isMicrophoneMute

        tile.label = if (isMuted) "מיקרופון מושתק" else "חוסם מיקרופון"
        tile.state = if (isMuted) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isMuted) "חסום" else "פעיל"
        }
        tile.updateTile()
    }
}

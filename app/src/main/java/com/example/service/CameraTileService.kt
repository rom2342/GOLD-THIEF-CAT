package com.example.service

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.example.MainActivity
import com.example.R
import com.example.receiver.DeviceAdminBlockReceiver

@RequiresApi(Build.VERSION_CODES.N)
class CameraTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(this, DeviceAdminBlockReceiver::class.java)

        if (!dpm.isAdminActive(admin)) {
            // Need device admin, prompt user to open app
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+ startActivityAndCollapse needs pending intent or standard call
                startActivityAndCollapse(intent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
            return
        }

        val currentlyBlocked = dpm.getCameraDisabled(admin)
        val shouldBlock = !currentlyBlocked
        try {
            dpm.setCameraDisabled(admin, shouldBlock)
        } catch (_: Exception) {}

        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(this, DeviceAdminBlockReceiver::class.java)

        val isBlocked = dpm.isAdminActive(admin) && dpm.getCameraDisabled(admin)

        tile.label = if (isBlocked) "מצלמה חסומה" else "חוסם מצלמה"
        tile.state = if (isBlocked) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isBlocked) "נעולה" else "פתוחה"
        }
        tile.updateTile()
    }
}

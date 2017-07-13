package com.dede.oneplusscreen.tile

import android.preference.PreferenceManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.dede.oneplusscreen.ELECTRONIC_INK
import com.dede.oneplusscreen.util.Util

/**
 * @author hsh
 * @time 2017/7/7 007 下午 01:11.
 * @doc
 */
class ElectronicInkTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        var state = Util.getSimulateColorSpaceState(applicationContext)
        qsTile.state = if (state) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        var state = Util.getSimulateColorSpaceState(applicationContext)
        val result = Util.setSimulateColorSpaceState(applicationContext, !state, null)
        if (result) {
            val sp = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            sp.edit().putBoolean(ELECTRONIC_INK, !state).apply()
            qsTile.state = if (!state) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            qsTile.updateTile()
        }
    }
}
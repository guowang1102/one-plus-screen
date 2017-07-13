package com.dede.oneplusscreen.tile

import android.content.Intent
import android.service.quicksettings.TileService
import android.view.WindowManager
import com.dede.oneplusscreen.activity.SettingActivity

/**
 * @author hsh
 * @time 2017/7/7 007 下午 02:17.
 * @doc
 */
class SettingTileService : TileService() {

    override fun onClick() {
        super.onClick()
        val intent = Intent(applicationContext, SettingActivity::class.java)
        if (isLocked) {
            intent.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
        startActivityAndCollapse(intent)
    }
}
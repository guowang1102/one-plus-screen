package com.dede.oneplusscreen.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.widget.Toast
import com.dede.oneplusscreen.ADOBE_RGB
import com.dede.oneplusscreen.DCI_P3
import com.dede.oneplusscreen.S_RGB
import com.dede.oneplusscreen.util.ShellUtil
import org.jetbrains.anko.AnkoLogger

/**
 * @author hsh
 * @time 2017/6/25 025 8:34 上午.
 * @doc
 */
class BootBroadcastReceiver : BroadcastReceiver(), AnkoLogger {

    override fun onReceive(context: Context, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent?.action)) {
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            var state = sp.getBoolean(S_RGB, false)
            if (state) {
                ShellUtil.setState(state, S_RGB)
                Toast.makeText(context, "开启" + S_RGB + "模式", Toast.LENGTH_LONG).show()
                return
            }

            state = sp.getBoolean(DCI_P3, false)
            if (state) {
                ShellUtil.setState(state, DCI_P3)
                Toast.makeText(context, "开启" + DCI_P3 + "模式", Toast.LENGTH_LONG).show()
                return
            }

            state = sp.getBoolean(ADOBE_RGB, false)
            if (state) {
                ShellUtil.setState(state, ADOBE_RGB)
                Toast.makeText(context, "开启" + ADOBE_RGB + "模式", Toast.LENGTH_LONG).show()
                return
            }
        }
    }
}
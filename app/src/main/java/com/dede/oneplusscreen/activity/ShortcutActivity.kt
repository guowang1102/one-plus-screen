package com.dede.oneplusscreen.activity

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.dede.oneplusscreen.*
import com.dede.oneplusscreen.util.ShellUtil
import com.dede.oneplusscreen.util.Util

/**
 * @author hsh
 * @time 2017/6/24 024 3:17 下午.
 * @doc
 */
class ShortcutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val type = intent.extras.getString("type")
        var sp = PreferenceManager.getDefaultSharedPreferences(this@ShortcutActivity)
        val editor = sp.edit()
        when (type) {
            S_RGB, DCI_P3, ADOBE_RGB -> {
                val state = sp.getBoolean(type, false)
                ShellUtil.setState(!state, type)
                editor.putBoolean(type, !state).apply()
            }
            ELECTRONIC_INK -> {
                val state = sp.getBoolean(type, false)
                Util.setSimulateColorSpaceState(this, !state, null)
                editor.putBoolean(type, !state).apply()
            }
        }
        finish()
    }
}

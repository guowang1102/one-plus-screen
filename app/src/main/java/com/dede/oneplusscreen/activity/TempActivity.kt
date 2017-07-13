package com.dede.oneplusscreen.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.startActivity

/**
 * @author hsh
 * @time 2017/6/24 024 2:18 下午.
 * @doc 过渡界面用于隐藏桌面图标，如果隐藏桌面图标此Activity会不可用
 */
class TempActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity<SettingActivity>()
        finish()
    }
}

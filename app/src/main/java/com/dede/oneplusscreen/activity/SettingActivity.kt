package com.dede.oneplusscreen.activity

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.preference.PreferenceManager
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.ViewTreeObserver
import com.dede.oneplusscreen.*
import com.dede.oneplusscreen.data.SaveInfo
import com.dede.oneplusscreen.fragment.SettingFragment
import com.dede.oneplusscreen.util.ShellUtil
import com.dede.oneplusscreen.util.Util
import com.dede.oneplusscreen.util.Util.loadModeState
import kotlinx.android.synthetic.main.activity_setting.*
import org.jetbrains.anko.browse
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread

/**
 * @author hsh
 * @time 2017/6/23 023 下午 02:20.
 * @doc
 */
class SettingActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var settingFragment: SettingFragment

    var clickCount = 0

    lateinit var sp: SharedPreferences

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        window.statusBarColor = Color.TRANSPARENT

        sp = PreferenceManager.getDefaultSharedPreferences(this)

        setSupportActionBar(tool_bar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toggle = ActionBarDrawerToggle(this, drawer_layout, R.string.drawer_layout_open, R.string.drawer_layout_close)
        toggle.syncState()
        drawer_layout.addDrawerListener(toggle)

        val layoutParams = navigation_view.layoutParams
        layoutParams.width = (resources.displayMetrics.widthPixels * 3.2f / 5 + .5f).toInt()
        navigation_view.layoutParams = layoutParams//修改侧滑菜单的宽度

        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            navigation_view.menu.findItem(R.id.version).title = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        navigation_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.url -> {
                    browse(getString(R.string.url), true)
                }
            }
            return@setNavigationItemSelectedListener true
        }

        content.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                loadSwitchState()//界面加载完成时再加载权限信息

                val headerView = navigation_view.getHeaderView(0)
                headerView.findViewById(R.id.icon).setOnClickListener {
                    val state = sp.getBoolean(IMAGE_STATE, true)
                    if (state) return@setOnClickListener

                    clickCount++
                    if (clickCount >= 2) {
                        sp.edit().putBoolean(IMAGE_STATE, true).apply()
                        settingFragment.setImagePreferenceState(true)
                        clickCount = 0
                    } else if (clickCount in 0..1) {
                        toast("再次点击恢复样例图片显示")
                    }
                }
                content.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        settingFragment = SettingFragment()
        fragmentManager.beginTransaction()
                .replace(R.id.content, settingFragment)
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            toggle.onOptionsItemSelected(item)
        return super.onOptionsItemSelected(item)
    }

    fun loadSwitchState() {
        doAsync {
            val saveInfo = SaveInfo()

            val requestSU = ShellUtil.requestRoot()//请求root权限
            saveInfo.rootState = requestSU

            val lcdInfo = Util.loadLcdInfo()//加载屏幕信息
            saveInfo.lcdInfo = lcdInfo

            //赋予修改安全设置的权限，慎用此权限。
            ShellUtil.exeCmdAsync("pm grant $packageName android.permission.WRITE_SECURE_SETTINGS")

            val editor = PreferenceManager.getDefaultSharedPreferences(this@SettingActivity).edit()

            var state: Boolean = false
            if (sRgbFile.exists() && sRgbFile.canRead()) {
                saveInfo.sRgbEnabled = true
                state = loadModeState(sRgbFile)
                saveInfo.sRbgState = state
                editor.putBoolean(S_RGB, state)
            }

            if (dciP3File.exists() && dciP3File.canRead()) {
                saveInfo.dciP3Enabled = true
                state = loadModeState(dciP3File)
                saveInfo.dciP3State = state
                editor.putBoolean(DCI_P3, state)
            }

            if (adobeRgbFile.exists() && adobeRgbFile.canRead()) {
                saveInfo.adobeRgbEnabled = true
                state = loadModeState(adobeRgbFile)
                saveInfo.adobeRgbState = state
                editor.putBoolean(ADOBE_RGB, state)
            }

            state = Util.getSimulateColorSpaceState(this@SettingActivity)
            saveInfo.electronicInkState = state
            editor.putBoolean(ELECTRONIC_INK, state)
            editor.apply()

            val imageState = sp.getBoolean(IMAGE_STATE, true)//图片显示状态
            saveInfo.imageState = imageState

            uiThread {
                toast("初始化完成")
                settingFragment.setViewState(saveInfo)
            }
        }
    }

    var lastTime: Long = 0
    override fun onBackPressed() {
        val first = System.currentTimeMillis()
        if ((lastTime + 2000) > first)
            super.onBackPressed()
        else {
            toast("再按一次退出")
            lastTime = first
        }
    }

}
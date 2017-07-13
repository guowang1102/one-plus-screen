package com.dede.oneplusscreen.util

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import com.dede.oneplusscreen.activity.TempActivity
import java.io.*

/**
 * @author hsh
 * @time 2017/6/25 025 7:25 下午.
 * @doc
 */
object Util {

    /**
     * 读取显示模式状态，mode = 1，表示开启
     */
    fun loadModeState(file: File): Boolean {
        if (!file.exists() || !file.canRead())
            return false
        var br: BufferedReader? = null
        try {
            br = BufferedReader(InputStreamReader(FileInputStream(file)))
            val firstLine = br.readLine()
            return firstLine.endsWith("1")
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            try {
                br?.close()
            } catch (e: IOException) {
            }
        }
    }

    /**
     * 加载屏幕信息
     */
    fun loadLcdInfo(): String? {
        val file = File("/sys/project_info/component_info/lcd")
        if (!file.exists() || !file.canRead())
            return null
        var br: BufferedReader? = null
        try {
            br = BufferedReader(InputStreamReader(FileInputStream(file)))
            //VER:  S6E3FA5   VER:  S6E3FA3
            val value = br.readLine().replace("VER:\t", "")
            return value
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            try {
                br?.close()
            } catch (e: IOException) {
            }
        }
    }

    fun openACTByName(context: Context, packageName: String, className: String, callBack: ExceptionCallBack?) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setClassName(packageName, className)
        openACTByIntent(context, intent, callBack)
    }

    fun openACTByIntent(context: Context, intent: Intent, callBack: ExceptionCallBack?) {
        try {
            context.startActivity(intent)
        } catch(e: ActivityNotFoundException) {//未找到Activity异常保护
            callBack?.onException(e)
            e.printStackTrace()
        } catch (e: Exception) {
            callBack?.onException(e)
            e.printStackTrace()
        }
    }

    interface ExceptionCallBack {
        fun onException(e: Exception)
    }


    fun getSimulateColorSpaceState(context: Context): Boolean {
        val cr = context.contentResolver
        try {
            val state = Settings.Secure.getInt(cr, "accessibility_display_daltonizer_enabled")
            if (state == 0)
                return false
            else {
                val value = Settings.Secure.getInt(cr, "accessibility_display_daltonizer")
                return value == 0
            }
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * 设置模拟颜色空间的状态
     *
     * @author hsh
     * @param context
     * @param state
     * @param callBack
     * @return true 成功，false 失败
     */
    fun setSimulateColorSpaceState(context: Context, state: Boolean, callBack: ExceptionCallBack?): Boolean {
        val cr = context.contentResolver
        try {
            /**
             * 系统隐藏常量
             * android api 25源码 android.provider.Settings 5313行
             * android.provider.Settings.Secure
             * 是否启用模拟颜色空间
             * 0 停用, 1 启用
             * public static final String ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED = "accessibility_display_daltonizer_enabled"
             * 模拟颜色空间模式
             * 0 全色盲, 1 红色弱视, 2 绿色弱视, 3 蓝色弱视
             * public static final String ACCESSIBILITY_DISPLAY_DALTONIZER ="accessibility_display_daltonizer"
             */
            if (state) {
                Settings.Secure.putInt(cr, "accessibility_display_daltonizer_enabled", 1)
                Settings.Secure.putInt(cr, "accessibility_display_daltonizer", 0)
            } else {
                Settings.Secure.putInt(cr, "accessibility_display_daltonizer_enabled", 0)
            }
            return true
        } catch (e: SecurityException) {
            e.printStackTrace()
            callBack?.onException(e)
            return false
        }
    }

    /**
     * 设置图标显示状态
     *
     * @author hsh
     * @param newState
     * @param context
     */
    fun setIconState(context: Context, newState: Boolean) {
        val packageManager = context.packageManager
        val componentName = ComponentName(context, TempActivity::class.java)
        val thisState = packageManager.getComponentEnabledSetting(componentName)

        //当前状态与存储状态不同时
        if (thisState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED && newState) {//显示图标
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    PackageManager.DONT_KILL_APP)
        } else if (thisState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && !newState) {//隐藏图标
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP)
        }
    }
}

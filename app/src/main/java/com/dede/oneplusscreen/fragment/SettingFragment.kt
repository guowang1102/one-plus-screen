package com.dede.oneplusscreen.fragment

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.provider.MediaStore
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ListView
import android.widget.Toast
import com.dede.oneplusscreen.*
import com.dede.oneplusscreen.activity.ShortcutActivity
import com.dede.oneplusscreen.custom.ImagePreference
import com.dede.oneplusscreen.data.SaveInfo
import com.dede.oneplusscreen.util.ShellUtil
import com.dede.oneplusscreen.util.Util
import org.jetbrains.anko.dip
import org.jetbrains.anko.toast
import java.io.File
import java.util.*


/**
 * @author hsh
 * @time 2017/6/23 023 下午 02:42.
 * @doc
 */
class SettingFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    lateinit var sRgb: SwitchPreference
    lateinit var dciP3: SwitchPreference
    lateinit var adobeRgb: SwitchPreference
    lateinit var electronicInk: SwitchPreference
    lateinit var image: ImagePreference

    var clickCount = 0

    lateinit var shortcutInfos: ArrayList<ShortcutInfo>

    lateinit var sp: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.setting)
        sp = preferenceScreen.sharedPreferences
        sRgb = findPreference(S_RGB) as SwitchPreference
        dciP3 = findPreference(DCI_P3) as SwitchPreference
        adobeRgb = findPreference(ADOBE_RGB) as SwitchPreference
        electronicInk = findPreference(ELECTRONIC_INK) as SwitchPreference
        image = findPreference(IMAGE) as ImagePreference

        if (!(sp.getBoolean(IMAGE_STATE, true))) {
            preferenceScreen.removePreference(image)
        } else {
            setImagePreferenceImage()
        }

        findPreference(BATTERY_OPTIMIZATIONS).onPreferenceClickListener = this@SettingFragment
        findPreference(BOOT_MANAGER).onPreferenceClickListener = this@SettingFragment
        findPreference(SIMULATE_COLOR_SPACE).onPreferenceClickListener = this@SettingFragment
    }

    val permissionArray = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    val PERMISSION_REQUEST_CODE = 100
    val ACTIVITY_REQUEST_CODE = 200

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && permissions != null && grantResults != null) {//检查请求码
            if (grantResults.size != permissionArray.size || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                toast("您未授予读取存储空间权限")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITY_REQUEST_CODE && data != null) {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor = context.contentResolver.query(selectedImage,
                    filePathColumn, null, null, null)
            cursor.moveToFirst()

            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()
            image.setImageDrawable(BitmapDrawable.createFromPath(picturePath), true)
            sp.edit().putString(IMAGE_PATH, picturePath).apply()
        } else if (requestCode == ACTIVITY_REQUEST_CODE) {
            val imagePath = sp.getString(IMAGE_PATH, null)
            if (!imagePath.isNullOrEmpty()) {
                image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.img_test_bg), true)
                sp.edit().remove(IMAGE_PATH).apply()//删除照片路径
            }
        }
    }

    /**
     * 设置样例图片状态
     *
     * @author hsh
     * @time 2017/7/10 010 下午 05:04
     * @param state true 显示并设置点击和长按事件 ，false 隐藏
     */
    fun setImagePreferenceState(state: Boolean) {
        if (!state) {
            image.startHideAnim(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    image.setImageDrawable(ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent)))
                    preferenceScreen.removePreference(image)
                }
            })
        } else {
            preferenceScreen.addPreference(image)
            initImagePreference()
        }
    }

    private fun setImagePreferenceImage() {
        val imagePath = sp.getString(IMAGE_PATH, null)
        if (!imagePath.isNullOrEmpty()) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissionArray, PERMISSION_REQUEST_CODE)
            } else {
                val file = File(imagePath)
                if (file.exists() && file.isFile && file.length() > 0) {
                    image.setImageDrawable(BitmapDrawable.createFromPath(imagePath))
                } else
                    toast("读取样例图片错误")
            }
        } else {
            image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.img_test_bg))
        }
    }

    private fun initImagePreference() {
        contentView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                setImagePreferenceImage()
                image.startShowAnim()
                contentView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        image.setOnClickListener(View.OnClickListener {
            clickCount++
            if (clickCount >= 5) {
                setImagePreferenceState(false)
                sp.edit().putBoolean(IMAGE_STATE, false).apply()
                toast("下次不再显示")
                clickCount = 0
            } else if (clickCount in 2..3) {
                toast("不要再舔屏了！！！")
            }
        })

        image.setOnLongClickListener(View.OnLongClickListener {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissionArray, PERMISSION_REQUEST_CODE)
            } else {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, ACTIVITY_REQUEST_CODE)
            }
            false
        })
    }

    lateinit var contentView: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.contentView = view
        val listView = contentView.findViewById(android.R.id.list) as ListView//PreferenceFragment内部维护了一个ListView
        listView.dividerHeight = dip(.8f)
    }

    fun setViewState(saveInfo: SaveInfo) {
        if (saveInfo.rootState) {
            if (saveInfo.lcdInfo.isNullOrEmpty()) {
                findPreference(SCREEN_MODE).title = "非A5屏幕，部分功能可能无法使用"
            } else if (!"S6E3FA5".equals(saveInfo.lcdInfo)) {
                findPreference(SCREEN_MODE).title = "读取屏幕信息失败，部分功能可能无法使用"
            }
            findPreference(NORMAL).onPreferenceClickListener = this@SettingFragment
        } else {
            toast("获取root权限失败")
            findPreference(SCREEN_MODE).title = "获取root权限失败"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            shortcutInfos = ArrayList<ShortcutInfo>()

        if (saveInfo.sRgbEnabled) {
            sRgb.isEnabled = true
            addShortcut(S_RGB)
        }

        if (saveInfo.dciP3Enabled) {
            dciP3.isEnabled = true
            addShortcut(DCI_P3)
        }

        if (saveInfo.adobeRgbEnabled) {
            adobeRgb.isEnabled = true
            addShortcut(ADOBE_RGB)
        }

        if (saveInfo.imageState)
            initImagePreference()

        addShortcut(ELECTRONIC_INK)//shortcut最多添加4个，但是shortcutManager.maxShortcutCountPerActivity却返回5

        setShortcuts()

        Util.setIconState(context, saveInfo.iconState)
    }

    /**
     * 添加长按Shortcut条目
     */
    private fun addShortcut(type: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val intent = Intent(context, ShortcutActivity::class.java).putExtra("type", type)
            //shortcut点击事件只能跳转到一个页面，所以这里采用一个没有动画透明的activity作为目标页面，在页面内修改过模式后关闭自身
            intent.action = Intent.ACTION_VIEW
            var info = ShortcutInfo.Builder(context, type)
                    .setShortLabel(type)
                    .setLongLabel(type)
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_arrow2right))
                    .setIntent(intent)
                    .build()
            shortcutInfos.add(info)
        }
    }

    /**
     * 动态设置图标长按列表
     */
    fun setShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            if (sp.getBoolean(ICON_STATE, false)) {
                var shortcutManager = context.getSystemService(ShortcutManager::class.java)
                shortcutManager.dynamicShortcuts = shortcutInfos
            }
        }
    }

    override fun onStart() {
        super.onStart()
        sp.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        sp.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        when (preference?.key) {
            NORMAL -> {
                val editor = sp.edit()
                editor.putBoolean(S_RGB, false)
                editor.putBoolean(DCI_P3, false)
                editor.putBoolean(ADOBE_RGB, false)
                editor.putBoolean(ELECTRONIC_INK, false)
                editor.apply()
            }
            BATTERY_OPTIMIZATIONS -> {
                var powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                    startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                            .setData(Uri.parse("package:" + context.packageName)))
                } else {
                    toast("已经关闭电池优化了\n如何恢复：设置-电池-电池优化")
                }
            }
            BOOT_MANAGER -> {//不保证所有ROM都管用
                val packageName = "com.oneplus.security"
                val className = "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
                Util.openACTByName(context, packageName, className, object : Util.ExceptionCallBack {
                    override fun onException(e: Exception) {
                        if (e is ActivityNotFoundException) toast("没有找到系统开机自启管理")
                    }
                })
            }
            SIMULATE_COLOR_SPACE -> {
                val packageName = "com.android.settings"
                val className = "com.android.settings.DevelopmentSettings"//开发者选项
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setClassName(packageName, className)
                /**
                 * 模拟系统设置搜索点击跳转，查看系统设置源码
                 * http://androidxref.com/7.1.1_r6/xref/packages/apps/Settings/src/com/android/settings/SettingsPreferenceFragment.java#mPreferenceKey
                 *
                 * 195    @Override
                 * 196    public void onResume() {
                 * 197        super.onResume();
                 * 198
                 * 199        final Bundle args = getArguments();
                 * 200        if (args != null) {
                 * 201            mPreferenceKey = args.getString(SettingsActivity.EXTRA_FRAGMENT_ARG_KEY);//取出参数key
                 * 202            highlightPreferenceIfNeeded();//调用滚动方法，内部调用了409行highlightPreference(key)实现滚动到指定key
                 * 203        }
                 * 204    }
                 */
                intent.putExtra(":settings:fragment_args_key", "simulate_color_space")//滚动到模拟颜色空间的位置（氢氧ROM自动滚动功能无效）
                Util.openACTByIntent(context, intent, object : Util.ExceptionCallBack {
                    override fun onException(e: Exception) {
                        toast("开发者选项打开失败")
                    }
                })
            }
        }
        return true
    }

    override fun onSharedPreferenceChanged(sp: SharedPreferences, key: String) {
        var value = sp.getBoolean(key, false)
        val editor = sp.edit()
        when (key) {
            S_RGB -> {
                if (sRgb.isChecked != value)
                    sRgb.isChecked = value
                ShellUtil.setState(value, key)
                if (value) {
                    editor.putBoolean(DCI_P3, false)
                    editor.putBoolean(ADOBE_RGB, false)
                    editor.apply()
                }
            }
            DCI_P3 -> {
                if (dciP3.isChecked != value)
                    dciP3.isChecked = value
                ShellUtil.setState(value, key)
                if (value) {
                    editor.putBoolean(ADOBE_RGB, false)
                    editor.putBoolean(S_RGB, false)
                    editor.apply()
                }
            }
            ADOBE_RGB -> {
                if (adobeRgb.isChecked != value)
                    adobeRgb.isChecked = value
                ShellUtil.setState(value, key)
                if (value) {
                    editor.putBoolean(DCI_P3, false)
                    editor.putBoolean(S_RGB, false)
                    editor.apply()
                }
            }
            ELECTRONIC_INK -> {
                if (electronicInk.isChecked != value)//点击tile修改了设置
                    electronicInk.isChecked = value
                Util.setSimulateColorSpaceState(context, value, object : Util.ExceptionCallBack {
                    override fun onException(e: Exception) {
                        toast(e.message!!)
                    }
                })
            }
            ICON_STATE -> {
                value = sp.getBoolean(key, true)//图标默认值为true，显示状态
                Toast.makeText(context, "若未生效请重启桌面", Toast.LENGTH_SHORT).show()
                Util.setIconState(context, value)
            }
        }
    }
}
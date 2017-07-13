package com.dede.oneplusscreen.util

import com.dede.oneplusscreen.*
import org.jetbrains.anko.doAsync
import java.io.DataOutputStream
import java.io.File
import java.io.IOException

/**
 * @author hsh
 * @time 2017/6/24 024 2:33 下午.
 * @doc
 */
object ShellUtil {

    fun normal() {
        doAsync {
            close(DCI_P3)
            close(S_RGB)
            close(ADOBE_RGB)
        }
    }

    fun open(type: String) {
        if (File(PATH + type).exists()) {
            var state = true
            when (type) {
                S_RGB -> {
                    state = Util.loadModeState(sRgbFile)
                }
                DCI_P3 -> {
                    state = Util.loadModeState(dciP3File)
                }
                ADOBE_RGB -> {
                    state = Util.loadModeState(adobeRgbFile)
                }
            }
            if (!state)
                exeCmd(OPEN + PATH + type)
        }
    }

    fun close(type: String) {
        if (File(PATH + type).exists()) {
            var state = false
            when (type) {
                S_RGB -> {
                    state = Util.loadModeState(sRgbFile)
                }
                DCI_P3 -> {
                    state = Util.loadModeState(dciP3File)
                }
                ADOBE_RGB -> {
                    state = Util.loadModeState(adobeRgbFile)
                }
            }
            if (state)
                exeCmd(CLOSE + PATH + type)
        }
    }

    fun setState(state: Boolean, type: String) {
        if (!File(PATH + type).exists()) {
            return
        }
        doAsync {
            if (!state) {
                close(type)
                return@doAsync
            }

            when (type) {
                S_RGB -> {
                    close(DCI_P3)
                    close(ADOBE_RGB)
                }
                DCI_P3 -> {
                    close(ADOBE_RGB)
                    close(S_RGB)
                }
                ADOBE_RGB -> {
                    close(DCI_P3)
                    close(S_RGB)
                }
            }
            open(type)
        }
    }

    fun exeCmdAsync(cmd: String?) {
        doAsync { exeCmd(cmd) }
    }

    fun exeCmd(cmd: String?) {
        if (cmd == null || "" == cmd)
            return
        var process: Process? = null
        var os: DataOutputStream? = null
        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            os.writeBytes(cmd + "\n")
            os.flush()
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            try {
                os?.close()
            } catch (e: IOException) {
            }
        }
    }

    fun requestRoot(): Boolean {
        var process: Process? = null
        var os: DataOutputStream? = null
        var waitFor: Int = -1
        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            os.writeBytes("exit\n")
            os.flush()
            waitFor = process.waitFor()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } catch (e: InterruptedException) {
            e.printStackTrace()
            return false
        } finally {
            try {
                os?.close()
            } catch (e: IOException) {
            }
        }
        return waitFor == 0
    }
}
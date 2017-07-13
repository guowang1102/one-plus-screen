package com.dede.oneplusscreen

import java.io.File

/**
 * @author hsh
 * @time 2017/6/24 024 1:33 下午.
 * @doc
 */

//sp key
val DCI_P3 = "DCI_P3"
val S_RGB = "SRGB"
val ADOBE_RGB = "Adobe_RGB"
val ELECTRONIC_INK = "Electronic_Ink"
val ICON_STATE = "icon_state"
val IMAGE_STATE = "image_state"
val IMAGE_PATH = "image_path"

val SCREEN_MODE = "screen_mode"
val NORMAL = "normal"
val BATTERY_OPTIMIZATIONS = "battery_optimizations"
val BOOT_MANAGER = "boot_manager"
val IMAGE = "image"
val SIMULATE_COLOR_SPACE = "simulate_color_space"

val PATH = "/sys/devices/virtual/graphics/fb0/"

val OPEN = "echo 1 > "
val CLOSE = "echo 0 > "

val sRgbFile = File(PATH + S_RGB)
val dciP3File = File(PATH + DCI_P3)
val adobeRgbFile = File(PATH + ADOBE_RGB)
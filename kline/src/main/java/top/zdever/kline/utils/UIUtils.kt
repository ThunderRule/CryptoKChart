package top.zdever.kline.utils

import android.content.res.Resources



/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/10
 */

private val displayMetrics = Resources.getSystem().displayMetrics
private val density = displayMetrics.density

internal val Int.dp:Int
    get() = (this * density + 0.5f).toInt()

internal val Float.dp:Float
    get() = this * density + 0.5f

val Float.sp:Float
    @JvmName("spToPx")
    get() = this * displayMetrics.scaledDensity+0.5f

val Int.sp:Int
    @JvmName("spToPx")
    get() = (this * displayMetrics.scaledDensity+0.5f).toInt()
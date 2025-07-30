package top.zdever.kline.utils

import android.content.Context
import android.content.res.Resources
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat


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

fun Context.color(@ColorRes color:Int) = ContextCompat.getColor(this,color)
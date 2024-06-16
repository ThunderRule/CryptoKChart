package top.zdever.kline.utils

import android.util.Log

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/11
 */
internal var isDebug:Boolean = true

internal fun Any?.logd(tag:String = "ChartView"){
    if (isDebug){
        Log.d(tag, this.toString())
    }
}
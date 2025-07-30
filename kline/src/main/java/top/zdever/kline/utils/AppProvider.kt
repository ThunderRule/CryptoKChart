package top.zdever.kline.utils

import android.annotation.SuppressLint
import android.app.Application

/**
 * @description
 * 通过放射获取applicant context
 *
 * @author bitman
 * @createDate 2023/6/9
 */
internal object AppProvider {

    @SuppressLint("PrivateApi")
    fun getAppContext(): Application = try {
        Class.forName("android.app.AppGlobals").getMethod("getInitialApplication")
            .invoke(null) as Application
    } catch (e: Exception) {
        Class.forName("android.app.ActivityThread").getMethod("currentApplication")
            .invoke(null) as Application
    }

}
package top.zdever.kline.format

import android.annotation.SuppressLint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/15
 */
class DefaultDateFormat : IDateFormat {
    @SuppressLint("SimpleDateFormat")
    private val mmddhhmm = SimpleDateFormat("MM-dd HH:mm")

    override fun format(date: Long): String {
        return mmddhhmm.format(Date(date))
    }
}
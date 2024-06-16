package top.zdever.kline.constants

import androidx.annotation.LongDef

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/13
 */
const val TIME_LINE = 2912L
const val TIME_1M = 60000L
const val TIME_3M = 180000L
const val TIME_5M = 300000L
const val TIME_15M = 900000L
const val TIME_30M = 1800000L
const val TIME_1H = 3600000L
const val TIME_2H = 7200000L
const val TIME_4H = 14400000L
const val TIME_6H = 21600000L
const val TIME_8H = 28800000L
const val TIME_12H = 43200000L
const val TIME_1D = 86400000L
const val TIME_1W = 604800000L
const val TIME_1MON = 2592000000L


@LongDef(TIME_LINE, TIME_1M, TIME_3M, TIME_5M, TIME_15M, TIME_30M, TIME_1H, TIME_2H, TIME_4H, TIME_6H, TIME_8H, TIME_12H, TIME_1D, TIME_1W, TIME_1MON)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class TimeType()

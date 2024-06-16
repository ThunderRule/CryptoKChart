package top.zdever.kline.constants

import androidx.annotation.IntDef

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/11
 */

const val MAIN_CANDLE = 212
const val MAIN_LINE = 213

@IntDef(MAIN_CANDLE, MAIN_LINE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class MainLineType
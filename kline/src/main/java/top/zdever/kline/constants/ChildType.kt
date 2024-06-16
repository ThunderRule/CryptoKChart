package top.zdever.kline.constants

import androidx.annotation.IntDef

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/11
 */

const val CHILD_MAIN = 2911
const val CHILD_KDJ = 2912
const val CHILD_VOLUME = 2913
const val CHILD_RSI = 2914
const val CHILD_MACD = 2915
const val CHILD_WR = 2916
const val CHILD_SAR = 2917

@IntDef(CHILD_MAIN,CHILD_KDJ, CHILD_MACD, CHILD_WR, CHILD_RSI, CHILD_VOLUME, CHILD_SAR)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class ChildType
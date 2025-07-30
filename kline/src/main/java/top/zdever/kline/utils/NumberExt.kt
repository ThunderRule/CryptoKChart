package top.zdever.kline.utils

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/22
 */
val Float.isExtremum:Boolean
    get() = this == Float.MIN_VALUE || this == Float.MAX_VALUE

val Double?.isExtremum:Boolean
    get() = this == null  || this == Double.MIN_VALUE || this == Double.MAX_VALUE || this.isNaN()
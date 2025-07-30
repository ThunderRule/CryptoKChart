package top.zdever.kline.model

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/22
 */
data class DrawLineEntity(
    val name:Long,
    val startTime:Long,
    val startValue:Float,
    var endTime:Long = Long.MIN_VALUE,
    var endValue:Float = Float.MIN_VALUE
)

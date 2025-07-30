package top.zdever.kline.constants

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/22
 */
object IndexType{
    const val MA = "Ma"
    const val EMA = "Ema"
    const val BOLL = "Bol"
    const val SAR = "Sar"
    const val VAL = "Val"

}

object ChartType{
    const val MAIN = "Main"
    const val VOL = "Vol"
    const val MACD = "Macd"
    const val KDJ = "Kdj"
    const val WR = "Wr"
    const val RSI = "Rsi"
    const val BOTTOM_ALL = "BottomALl"
}

enum class SelectType {
    SELECT_TOUCH,
    SELECT_PRESS,
    SELECT_BOTH,
    SELECT_NONE
}
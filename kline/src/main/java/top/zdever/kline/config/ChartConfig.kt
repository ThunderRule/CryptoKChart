package top.zdever.kline.config

import top.zdever.kline.constants.ChartType
import top.zdever.kline.constants.IndexType

/**
 * @description
 *
 * @author Draksum
 * @createDate 2025/7/28
 */
data class ChartConfig(
    var isOpenCountTimer: Boolean = false,
    var isOpenLastPrice:Boolean = true,
    var isOpenHisOrder: Boolean = true,
    var isOpenNowOrder: Boolean = false,
    var isOpenPosition:Boolean = false,
    var isOpenForcePrice:Boolean = false,
    var childList:ArrayList<String> = arrayListOf(ChartType.MAIN,ChartType.VOL),
    var childConfig:HashMap<String,IConfig> = hashMapOf()
)

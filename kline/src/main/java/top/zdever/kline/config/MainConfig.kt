package top.zdever.kline.config

import top.zdever.kline.constants.CandleStyle
import top.zdever.kline.constants.IndexType

/**
 * @description
 *
 * @author Draksum
 * @createDate 2025/7/28
 */
data class MainConfig(
    override val name: String = "Main",
    var indicator:String = IndexType.MA,
    var candleStyle: CandleStyle = CandleStyle.STYLE_FILL,
    var maConfig:List<LineConfig>,
    var emaConfig:List<LineConfig>,
    var bollConfig:BollConfig,
    var sarConfig:SarConfig
):IConfig{
    data class BollConfig(
        var cycle:Int,
        var bandWidth:Float,
        var legendColor:Int,
        var upConfig:LineConfig,
        var mbConfig:LineConfig,
        var dnConfig:LineConfig
    )

    data class SarConfig(
        var start:Float,
        var max:Float,
        var color:Int
    )
}

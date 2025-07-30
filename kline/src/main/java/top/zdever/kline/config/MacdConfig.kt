package top.zdever.kline.config

/**
 * @description
 *
 * @author Draksum
 * @createDate 2025/7/28
 */
data class MacdConfig(
    override val name: String,
    var shortCycle:Int,
    var longCycle:Int,
    var moveAvgCycle:Int,
    var legendColor:Int,
    var difConfig:LineConfig,
    var deaConfig:LineConfig,
    var upColor:Int,
    var downColor:Int,
    var longIncreaseFill:Boolean,
    var longDecreaseFill:Boolean,
    var shortIncreaseFill:Boolean,
    var shortDecreaseFill:Boolean
):IConfig

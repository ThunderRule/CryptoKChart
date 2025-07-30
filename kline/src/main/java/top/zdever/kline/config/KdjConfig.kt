package top.zdever.kline.config

/**
 * @description
 *
 * @author Draksum
 * @createDate 2025/7/28
 */
data class KdjConfig(
    override val name: String,
    var cycle:Int,
    var moveAvgCycle1:Int,
    var moveAvgCycle2:Int,
    var kConfig: LineConfig,
    var dConfig: LineConfig,
    var jConfig: LineConfig
):IConfig
package top.zdever.kline.config

/**
 * @description
 *
 * @author Draksum
 * @createDate 2025/7/28
 */
data class VolConfig(
    override val name: String,
    var maConfig:List<LineConfig>,
    var volTextColor:Int,
    var upIsFill:Boolean,
    var downIsFIll:Boolean
):IConfig

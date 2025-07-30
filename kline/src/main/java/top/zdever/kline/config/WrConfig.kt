package top.zdever.kline.config

/**
 * @description
 *
 * @author Draksum
 * @createDate 2025/7/29
 */
data class WrConfig(
    override val name: String,
    var cycle:Int,
    var width:Float,
    var color:Int,
    var isOpen:Boolean
) : IConfig
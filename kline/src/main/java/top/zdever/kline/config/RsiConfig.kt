package top.zdever.kline.config

/**
 * @description
 *
 * @author Draksum
 * @createDate 2025/7/29
 */
data class RsiConfig(
    override val name: String,
    val lineConfig:List<LineConfig>
):IConfig

package top.zdever.kline.calculate

import top.zdever.kline.base.IChartDraw
import top.zdever.kline.model.IKLine

/**
 * @description
 *
 * @author Draksum
 * @createDate 2025/7/15
 */
object CalculateManager {

    private val indicatorMap = HashMap<String,IChartDraw<*>>()

    fun register(indicator:IChartDraw<*>){
        indicatorMap[indicator.getDefaultConfig().name] = indicator
    }

    fun calculateAll(list: List<IKLine>){
        for ((i,v) in list.withIndex()) {
            for (entry in indicatorMap) {
                entry.value.calculate(i,v,list)
            }
        }
    }

}
package top.zdever.kline.utils

import org.json.JSONArray
import top.zdever.kline.constants.ChartType
import top.zdever.kline.model.DrawLineEntity

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/11
 */
object SavedUtils {
    private val cachedIndexes = arrayListOf<String>()
    private val cacheDrawTools = arrayListOf<DrawLineEntity>()

    fun saveIndexes(index: String) {
        cachedIndexes.add(index)
        val jsonArray = JSONArray()
        for (i in cachedIndexes) {
            jsonArray.put(i)
        }
        val json = jsonArray.toString()

    }

    fun getIndexes(): ArrayList<String> {
        cachedIndexes.add(ChartType.MAIN)
        cachedIndexes.add(ChartType.VOL)
        cachedIndexes.add(ChartType.KDJ)
        cachedIndexes.add(ChartType.WR)
//        cachedIndexes.add(ChartType.RSI)
        cachedIndexes.add(ChartType.MACD)
        return cachedIndexes
    }

    fun getDrawTools() = cacheDrawTools

    fun saveDrawTool(drawLineEntity: DrawLineEntity?){
        drawLineEntity?:return
        cacheDrawTools.add(drawLineEntity)
    }

}
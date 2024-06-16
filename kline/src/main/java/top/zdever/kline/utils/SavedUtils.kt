package top.zdever.kline.utils

import org.json.JSONArray
import org.json.JSONObject
import top.zdever.kline.constants.CHILD_KDJ
import top.zdever.kline.constants.CHILD_MACD
import top.zdever.kline.constants.CHILD_MAIN
import top.zdever.kline.constants.CHILD_RSI
import top.zdever.kline.constants.CHILD_SAR
import top.zdever.kline.constants.CHILD_VOLUME
import top.zdever.kline.constants.CHILD_WR

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/11
 */
object SavedUtils {
    private val cachedIndexes = arrayListOf<Int>()

    fun saveIndexes(index:Int){
        cachedIndexes.add(index)
        val jsonArray = JSONArray()
        for (i in cachedIndexes) {
            jsonArray.put(i)
        }
        val json = jsonArray.toString()

    }

    fun getIndexes():ArrayList<Int>{
        cachedIndexes.add(CHILD_MAIN)
        cachedIndexes.add(CHILD_VOLUME)
//        cachedIndexes.add(CHILD_KDJ)
        cachedIndexes.add(CHILD_WR)
//        cachedIndexes.add(CHILD_RSI)
//        cachedIndexes.add(CHILD_MACD)
//        cachedIndexes.add(CHILD_SAR)
        return cachedIndexes
    }

}
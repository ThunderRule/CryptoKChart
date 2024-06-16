package top.zdever.kline.utils

import top.zdever.kline.base.IDataTool
import top.zdever.kline.constants.CHILD_WR
import top.zdever.kline.constants.KEY_WR_1
import top.zdever.kline.model.IKLine
import kotlin.math.max

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/16
 */
class DefaultDataTool : IDataTool {
    override fun calculate(list: List<IKLine>) {
        var wr = 0f
        for ((i,point) in list.withIndex()) {
            var startIndex = i - 14
            if (startIndex < 0){
                startIndex = 0
            }
            var max14 = Float.MIN_VALUE
            var min14 = Float.MAX_VALUE
            for (index in startIndex .. i){
                max14 = max(max14,list[index].getHighPrice().toFloat())
                min14 = max(min14,list[index].getLowPrice().toFloat())
            }
            if (i < 13){
                point.setIndex(CHILD_WR, listOf(-10f))
            }else{
                wr = -100 *(max14 - point.getClosePrice().toFloat())/(max14 - min14)
                if (wr.isNaN()){
                    point.setIndex(CHILD_WR, listOf(0f))
                }else{
                    point.setIndex(CHILD_WR, listOf(wr))
                }
            }
        }
    }
}
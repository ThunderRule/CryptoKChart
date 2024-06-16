package top.zdever.kline.model

import top.zdever.kline.constants.ChildType

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/10
 */
interface IKLine {

    fun getOpenPrice():String

    fun getHighPrice():String

    fun getLowPrice():String

    fun getClosePrice():String

    fun getVolume():String

    fun getTime():Long

    /**
     * 各种指标
     */
    fun getIndex(@ChildType key:Int):List<Float>?

    fun setIndex(@ChildType key: Int,list:List<Float>)

}
package top.zdever.kline.model

import top.zdever.kline.constants.ChildType

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/10
 */
abstract class BaseKLineEntity : IKLine {
    private val indexMap = hashMapOf<Int,List<Float>>()

    abstract override fun getOpenPrice():String

    abstract override fun getHighPrice():String

    abstract override fun getLowPrice():String

    abstract override fun getClosePrice():String

    abstract override fun getVolume(): String

    abstract override fun getTime(): Long

    override fun getIndex(@ChildType key: Int) = indexMap[key]

    override fun setIndex(@ChildType key: Int, list: List<Float>) {
        indexMap[key] = list
    }
}
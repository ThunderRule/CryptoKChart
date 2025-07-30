package top.zdever.kline.model

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/10
 */
interface IKLine {

    fun getOpenPrice(): String

    fun getHighPrice(): String

    fun getLowPrice(): String

    fun getClosePrice(): String

    fun getVolume(): String

    fun getAmount():String

    fun getTime(): Long

    /**
     * 各种指标
     */
    fun getMainIndex(childType: String, id: Int): Double?

    fun getMainIndexList(childType: String):HashMap<Int,Double>?

    fun setMainIndex(childType: String, id: Int, value: Double)

    fun getChildIndex(childType: String, id: Int): Double?

    fun getChildIndexList(childType: String):HashMap<Int,Double>?

    fun setChildIndex(childType: String, id: Int, value: Double)

}
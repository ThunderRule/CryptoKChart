package top.zdever.kline.model

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/10
 */
abstract class BaseKLineEntity : IKLine {
    private val mainIndexCache = hashMapOf<String,HashMap<Int,Double>>()
    private val childIndexCache = hashMapOf<String,HashMap<Int,Double>>()

    abstract override fun getOpenPrice():String

    abstract override fun getHighPrice():String

    abstract override fun getLowPrice():String

    abstract override fun getClosePrice():String

    abstract override fun getVolume(): String

    abstract override fun getTime(): Long

    override fun getMainIndex(childType: String,id:Int) = mainIndexCache[childType]?.get(id)

    override fun getMainIndexList(childType: String) = mainIndexCache[childType]

    override fun setMainIndex(childType: String, id: Int, value: Double) {
        val map = mainIndexCache[childType]
        if (map == null){
            mainIndexCache[childType] = hashMapOf(id to value)
        }else{
            map[id] = value
        }
    }

    override fun getChildIndex(childType: String, id: Int) = childIndexCache[childType]?.get(id)

    override fun getChildIndexList(childType: String) = childIndexCache[childType]

    override fun setChildIndex(childType: String, id: Int, value: Double) {
        val map = childIndexCache[childType]
        if (map == null){
            childIndexCache[childType] = hashMapOf(id to value)
        }else{
            map[id] = value
        }
    }
}
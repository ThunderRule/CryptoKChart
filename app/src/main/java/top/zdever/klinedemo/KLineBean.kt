package top.zdever.klinedemo

import top.zdever.kline.model.BaseKLineEntity

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/15
 */
data class KLineBean(
    val o:String,
    val h:String,
    val l:String,
    val c:String,
    val vol:String,
    val date:Long,
) : BaseKLineEntity() {
    override fun getOpenPrice() = o

    override fun getHighPrice() = h

    override fun getLowPrice() = l

    override fun getClosePrice() = c

    override fun getVolume() = vol

    override fun getTime() = date

}
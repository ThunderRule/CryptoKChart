package top.zdever.kline.base

import android.database.DataSetObserver
import top.zdever.kline.constants.TimeType
import top.zdever.kline.model.IKLine

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/10
 */
interface IAdapter {

    fun getCount():Int

    fun getItem(position:Int):IKLine

    fun registerDataSetObserver(observer: DataSetObserver?)

    fun unregisterDataSetObserver(observer: DataSetObserver?)

    fun notifyDataSetChanged()

    fun addData(list:List<IKLine>?)

    fun addLast(data:IKLine?)
}
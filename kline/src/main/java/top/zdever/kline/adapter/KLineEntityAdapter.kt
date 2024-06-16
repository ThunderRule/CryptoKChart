package top.zdever.kline.adapter

import android.database.DataSetObservable
import android.database.DataSetObserver
import top.zdever.kline.base.IAdapter
import top.zdever.kline.constants.TimeType
import top.zdever.kline.model.BaseKLineEntity
import top.zdever.kline.model.IKLine
import top.zdever.kline.utils.DefaultDataTool

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/15
 */
class KLineEntityAdapter : IAdapter {
    private val mDataSetObservable = DataSetObservable()
    private val dataList = arrayListOf<IKLine>()
    private val dataTool = DefaultDataTool()

    override fun getCount() = dataList.size

    override fun getItem(position: Int) = dataList[position]

    override fun registerDataSetObserver(observer: DataSetObserver?) {
        if (observer != null) {
            mDataSetObservable.registerObserver(observer)
        }
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        if (observer != null) {
            mDataSetObservable.unregisterObserver(observer)
        }
    }

    override fun notifyDataSetChanged() {
        if (getCount() > 0) {
            mDataSetObservable.notifyChanged()
        } else {
            mDataSetObservable.notifyInvalidated()
        }
    }

    override fun addLast(data: IKLine?) {

    }


    override fun addData(list: List<IKLine>?) {
        dataList.clear()
        if (!list.isNullOrEmpty()){
            dataList.addAll(0,list)
            dataTool.calculate(dataList)
        }
        notifyDataSetChanged()
    }
}
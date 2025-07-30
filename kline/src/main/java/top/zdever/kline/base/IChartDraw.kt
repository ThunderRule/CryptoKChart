package top.zdever.kline.base

import android.graphics.Canvas
import top.zdever.kline.config.ChartConfig
import top.zdever.kline.config.ConfigManger
import top.zdever.kline.config.IConfig
import top.zdever.kline.format.DefaultValueFormat
import top.zdever.kline.format.IValueFormat
import top.zdever.kline.model.IKLine

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/10
 */
abstract class IChartDraw<C:IConfig> {
    private var mValueFormatter:IValueFormat = DefaultValueFormat()
    private var mConfig:C? = null

    protected abstract val name:String

    abstract fun draw(canvas: Canvas, prePoint: IKLine?, curPoint:IKLine?, preX:Float, curX:Float, position:Int, view: BaseKChartView)

    abstract fun drawText(canvas: Canvas,view: BaseKChartView,x:Float,y:Float,point: IKLine?)

    abstract fun getMaxValue(point:IKLine?):Double

    abstract fun getMinValue(point: IKLine?):Double

    abstract fun getDefaultConfig():C

    abstract fun calculate(index:Int,curPoint: IKLine,list:List<IKLine>)

    protected fun getConfig() = (ConfigManger.getConfig().childConfig[name] as? C)?:getDefaultConfig()

    fun setValueFormatter(valueFormatter: IValueFormat){
        mValueFormatter = valueFormatter
    }

    fun getValueFormatter() = mValueFormatter
}
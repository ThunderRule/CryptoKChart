package top.zdever.kline.base

import android.graphics.Canvas
import top.zdever.kline.format.IValueFormat
import top.zdever.kline.model.IKLine

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/10
 */
interface IChartDraw {

    fun draw(canvas: Canvas, prePoint: IKLine?, curPoint:IKLine?, preX:Float, curX:Float, position:Int, view: BaseKChartView)

    fun drawText(canvas: Canvas,view: BaseKChartView,position: Int,x:Float,y:Float)

    fun getMaxValue(point:IKLine?):Float

    fun getMinValue(point: IKLine?):Float

    fun getValueFormat():IValueFormat

}
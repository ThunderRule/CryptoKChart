package top.zdever.kline.draw

import android.content.Context
import android.graphics.Canvas
import top.zdever.kline.base.BaseKChartView
import top.zdever.kline.base.IChartDraw
import top.zdever.kline.constants.ChildType
import top.zdever.kline.format.DefaultValueFormat
import top.zdever.kline.format.IValueFormat
import top.zdever.kline.model.IKLine

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/11
 */
class MACDChart(private val context: Context):IChartDraw {
    override fun draw(
        canvas: Canvas,
        prePoint: IKLine?,
        curPoint: IKLine?,
        preX: Float,
        curX: Float,
        position: Int,
        view: BaseKChartView
    ) {

    }

    override fun drawText(canvas: Canvas, view: BaseKChartView, position: Int, x: Float, y: Float) {

    }

    override fun getMaxValue(point: IKLine?): Float {
        return 0f
    }

    override fun getMinValue(point: IKLine?): Float {
        return 0f
    }

    override fun getValueFormat(): IValueFormat {
        return DefaultValueFormat()
    }
}
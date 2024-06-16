package top.zdever.kline.draw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import top.zdever.kline.base.BaseKChartView
import top.zdever.kline.base.IChartDraw
import top.zdever.kline.constants.ChildType
import top.zdever.kline.format.DefaultValueFormat
import top.zdever.kline.format.IValueFormat
import top.zdever.kline.model.IKLine
import top.zdever.kline.utils.dp

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/11
 */
class VolumeChart(private val context: Context, private @ChildType val childType: Int) : IChartDraw {
    private val histogramPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(
        canvas: Canvas,
        prePoint: IKLine?,
        curPoint: IKLine?,
        preX: Float,
        curX: Float,
        position: Int,
        view: BaseKChartView
    ) {
        if (curPoint != null) {
            drawHistogram(
                canvas,
                curX,
                curPoint.getVolume().toFloat(),
                curPoint.getOpenPrice().toFloat(),
                curPoint.getClosePrice().toFloat(),
                view
            )
        }
    }

    private fun drawHistogram(
        canvas: Canvas,
        curX: Float,
        vol: Float,
        open: Float,
        close: Float,
        view: BaseKChartView
    ) {
        val diffWidth = (view.mPointWidth - 4) / 2 * view.scaleX
        val top = view.valueToY(childType,vol)
        val bottom = view.getRect(childType)!!.bottom
        if (close > open){
            histogramPaint.color = Color.GREEN
        }else{
            histogramPaint.color = Color.RED
        }
        canvas.drawRect(curX - diffWidth,top,curX + diffWidth, bottom.toFloat(),histogramPaint)
    }

    override fun drawText(canvas: Canvas, view: BaseKChartView, position: Int, x: Float, y: Float) {

    }

    override fun getMaxValue(point: IKLine?): Float {
        return point?.getVolume()?.toFloat() ?: Float.MIN_VALUE
    }

    override fun getMinValue(point: IKLine?): Float {
        return point?.getVolume()?.toFloat() ?: Float.MAX_VALUE
    }

    override fun getValueFormat(): IValueFormat {
        return DefaultValueFormat()
    }
}
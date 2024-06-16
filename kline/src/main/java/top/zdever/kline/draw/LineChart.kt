package top.zdever.kline.draw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import top.zdever.kline.base.BaseKChartView
import top.zdever.kline.base.IChartDraw
import top.zdever.kline.constants.CHILD_MAIN
import top.zdever.kline.constants.CHILD_WR
import top.zdever.kline.constants.ChildType
import top.zdever.kline.constants.KEY_WR_1
import top.zdever.kline.format.DefaultValueFormat
import top.zdever.kline.format.IValueFormat
import top.zdever.kline.model.IKLine

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/15
 */
class LineChart(private val context: Context,private @ChildType val childType:Int) : IChartDraw {
    private val mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(
        canvas: Canvas,
        prePoint: IKLine?,
        curPoint: IKLine?,
        preX: Float,
        curX: Float,
        position: Int,
        view: BaseKChartView
    ) {

        canvas.drawLine(
            preX,
            view.valueToY(childType, prePoint?.getIndex(childType)?.get(0) ?: 0f),
            curX,
            view.valueToY(childType, curPoint?.getIndex(childType)?.get(0) ?: 0f),
            mLinePaint
        )
    }

    override fun drawText(canvas: Canvas, view: BaseKChartView, position: Int, x: Float, y: Float) {

    }

    override fun getMaxValue(point: IKLine?): Float {
        point?:return Float.MIN_VALUE
        return point.getIndex(childType)?.get(0) ?:Float.MIN_VALUE
    }

    override fun getMinValue(point: IKLine?): Float {
        point?:return Float.MAX_VALUE
        return point.getIndex(childType)?.get(0) ?:Float.MAX_VALUE
    }

    override fun getValueFormat(): IValueFormat {
        return DefaultValueFormat()
    }
}
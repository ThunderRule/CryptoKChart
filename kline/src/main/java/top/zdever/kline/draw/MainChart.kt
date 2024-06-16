package top.zdever.kline.draw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import top.zdever.kline.base.BaseKChartView
import top.zdever.kline.base.IChartDraw
import top.zdever.kline.constants.CHILD_MAIN
import top.zdever.kline.constants.TIME_LINE
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
class MainChart(private val context: Context) : IChartDraw {
    private val mTimeLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mUpPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mDownPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        mTimeLinePaint.color = Color.RED
        mUpPaint.color = Color.GREEN
        mDownPaint.color = Color.RED
    }

    override fun draw(
        canvas: Canvas,
        prePoint: IKLine?,
        curPoint: IKLine?,
        preX: Float,
        curX: Float,
        position: Int,
        view: BaseKChartView
    ) {

        if (view.selectedTime == TIME_LINE) {
            canvas.drawLine(
                preX,
                view.valueToY(CHILD_MAIN, prePoint?.getClosePrice()?.toFloat() ?: 0f),
                curX,
                view.valueToY(CHILD_MAIN, curPoint?.getClosePrice()?.toFloat() ?: 0f),
                mTimeLinePaint
            )
        } else {
            if (curPoint != null) {
                drawCandle(
                    canvas,
                    curX,
                    curPoint.getHighPrice().toFloat(),
                    curPoint.getLowPrice().toFloat(),
                    curPoint.getOpenPrice().toFloat(),
                    curPoint.getClosePrice().toFloat(),
                    view
                )
            }
        }
    }

    private fun drawCandle(
        canvas: Canvas,
        x: Float,
        h: Float,
        l: Float,
        o: Float,
        c: Float,
        view: BaseKChartView
    ) {
        val hY = view.valueToY(CHILD_MAIN, h)
        val lY = view.valueToY(CHILD_MAIN, l)
        val oY = view.valueToY(CHILD_MAIN, o)
        val cY = view.valueToY(CHILD_MAIN, c)
        val width = (view.mPointWidth - 4) / 2 * view.scaleX
        if (oY < cY) {
            drawOneCandle(canvas, x, hY, lY, oY, cY, x - width, x + width, mDownPaint)
        } else if (oY > cY) {
            drawOneCandle(canvas, x, hY, lY, cY, oY, x - width, x + width, mUpPaint)
        } else {
            drawOneCandle(canvas, x, hY, lY, cY - 1, oY, x - width, x + width, mUpPaint)
        }
    }

    private fun drawOneCandle(
        canvas: Canvas,
        x: Float,
        h: Float,
        l: Float,
        o: Float,
        c: Float,
        leftWidth: Float,
        rightWidth: Float,
        paint: Paint
    ) {
        canvas.drawRect(leftWidth, o, rightWidth, c, paint)
        if (h < o) {
            canvas.drawLine(x, o, x, h, paint)
        }
        if (c < l) {
            canvas.drawLine(x, l, x, c, paint)
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKChartView, position: Int, x: Float, y: Float) {

    }

    override fun getMaxValue(point: IKLine?): Float {
        return point?.getHighPrice()?.toFloat() ?: Float.MIN_VALUE
    }

    override fun getMinValue(point: IKLine?): Float {
        return point?.getLowPrice()?.toFloat() ?: Float.MAX_VALUE
    }

    override fun getValueFormat(): IValueFormat {
        return DefaultValueFormat()
    }
}
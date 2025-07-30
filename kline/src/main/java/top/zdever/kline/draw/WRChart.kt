package top.zdever.kline.draw

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import top.zdever.kline.base.BaseKChartView
import top.zdever.kline.base.IChartDraw
import top.zdever.kline.config.KdjConfig
import top.zdever.kline.config.LineConfig
import top.zdever.kline.config.WrConfig
import top.zdever.kline.constants.ChartType
import top.zdever.kline.model.IKLine
import top.zdever.kline.utils.dp
import top.zdever.kline.utils.isExtremum

/**
 * @description
 *
 * @author Draksum
 * @createDate 2025/7/28
 */
class WRChart(override val name: String) : IChartDraw<WrConfig>() {
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(
        canvas: Canvas,
        prePoint: IKLine?,
        curPoint: IKLine?,
        preX: Float,
        curX: Float,
        position: Int,
        view: BaseKChartView
    ) {
        val config = getConfig()
        if (config.isOpen) {
            val preWr = prePoint?.getChildIndex(name, 1)
            val curWr = curPoint?.getChildIndex(name, 1)
            if (!preWr.isExtremum && !curWr.isExtremum) {
                linePaint.setColor(config.color)
                linePaint.strokeWidth = config.width
                canvas.drawLine(
                    preX,
                    view.valueToY(name, preWr!!.toFloat()),
                    curX,
                    view.valueToY(name, curWr!!.toFloat()),
                    linePaint
                )
            }
        }

    }

    override fun drawText(
        canvas: Canvas,
        view: BaseKChartView,
        x: Float,
        y: Float,
        point: IKLine?
    ) {
        point?:return
        val textPaint = view.legendTextPaint()
        var tempX = x
        val legend = "Wm %R(${getConfig().cycle}):"
        textPaint.color = getConfig().color
        canvas.drawText(legend,tempX,y,textPaint)
        tempX += textPaint.measureText(legend)
        if (getConfig().isOpen) {
            val formatWr = getValueFormatter().format(point.getChildIndex(name, 1)?.toString())
            canvas.drawText(formatWr,tempX,y,textPaint)
        }
    }

    override fun getMaxValue(point: IKLine?): Double {
        point ?: return Double.MIN_VALUE
        val max = point.getChildIndexList(name)?.values?.maxOrNull()
        return if (max.isExtremum) {
            Double.MIN_VALUE
        } else {
            max!!
        }
    }

    override fun getMinValue(point: IKLine?): Double {
        point ?: return Double.MAX_VALUE
        val min = point.getChildIndexList(name)?.values?.minOrNull()
        return if (min.isExtremum) {
            Double.MAX_VALUE
        } else {
            min!!
        }
    }

    override fun getDefaultConfig() = WrConfig(
        name = name,
        cycle = 14,
        width = 1f.dp,
        color = Color.CYAN,
        isOpen = true
    )

    override fun calculate(index: Int, curPoint: IKLine, list: List<IKLine>) {
        val cycle = getConfig().cycle
        if (index > cycle - 1){
            val window = list.subList(index - cycle + 1, index + 1)
            val highestHigh = window.maxOf { it.getHighPrice().toDouble() }
            val lowestLow = window.minOf { it.getLowPrice().toDouble() }
            val close = curPoint.getClosePrice().toDouble()

            val wr = -100 * (highestHigh - close)/(highestHigh - lowestLow)
            curPoint.setChildIndex(name,1,wr)
        }
    }
}
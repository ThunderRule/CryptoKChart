package top.zdever.kline.draw

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import top.zdever.kline.base.BaseKChartView
import top.zdever.kline.base.IChartDraw
import top.zdever.kline.config.LineConfig
import top.zdever.kline.config.RsiConfig
import top.zdever.kline.model.IKLine
import top.zdever.kline.utils.dp
import top.zdever.kline.utils.isExtremum

/**
 * @description
 *
 * @author Draksum
 * @createDate 2025/7/29
 */
class RsiChart(override val name: String) : IChartDraw<RsiConfig>() {
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
        for (lineConfig in config.lineConfig) {
            if (lineConfig.isOpen) {
                val preValue = prePoint?.getChildIndex(name, lineConfig.id)
                val curValue = curPoint?.getChildIndex(name, lineConfig.id)
                if (!preValue.isExtremum && !curValue.isExtremum) {
                    linePaint.setColor(lineConfig.color)
                    linePaint.strokeWidth = lineConfig.width
                    canvas.drawLine(
                        preX,
                        view.valueToY(name, preValue!!.toFloat()),
                        curX,
                        view.valueToY(name, curValue!!.toFloat()),
                        linePaint
                    )
                }
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

    override fun getDefaultConfig() = RsiConfig(
        name = name,
        lineConfig = listOf(
            LineConfig(name,1,6,1f.dp,Color.MAGENTA,true)
        )
    )

    override fun calculate(index: Int, curPoint: IKLine, list: List<IKLine>) {
        
    }
}
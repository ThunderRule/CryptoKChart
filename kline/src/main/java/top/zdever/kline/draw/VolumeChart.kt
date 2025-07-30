package top.zdever.kline.draw

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import top.zdever.kline.base.BaseKChartView
import top.zdever.kline.base.IChartDraw
import top.zdever.kline.config.LineConfig
import top.zdever.kline.config.VolConfig
import top.zdever.kline.constants.ChartType
import top.zdever.kline.constants.IndexType
import top.zdever.kline.model.IKLine
import top.zdever.kline.utils.dp
import top.zdever.kline.utils.isExtremum
import kotlin.math.max
import kotlin.math.min

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/11
 */
class VolumeChart(override val name: String) : IChartDraw<VolConfig>() {
    private val histogramPaint = Paint(Paint.ANTI_ALIAS_FLAG)
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
        val lineConfigs = getConfig().maConfig
        for (lineConfig in lineConfigs) {
            if (prePoint != null && curPoint != null) {
                val preIndexes = prePoint.getChildIndex(ChartType.VOL, lineConfig.id)
                val curIndexes = curPoint.getChildIndex(ChartType.VOL, lineConfig.id)
                if (preIndexes.isExtremum || curIndexes.isExtremum) {
                    continue
                }
                linePaint.color = lineConfig.color
                linePaint.strokeWidth = lineConfig.width
                canvas.drawLine(
                    preX,
                    view.valueToY(ChartType.VOL, preIndexes!!.toFloat()),
                    curX,
                    view.valueToY(ChartType.VOL, curIndexes!!.toFloat()),
                    linePaint
                )
            }
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
        val top = view.valueToY(ChartType.VOL, vol)
        val bottom = view.getRect(ChartType.VOL)!!.bottom
        if (close > open) {
            histogramPaint.color = Color.GREEN
        } else {
            histogramPaint.color = Color.RED
        }
        canvas.drawRect(curX - diffWidth, top, curX + diffWidth, bottom.toFloat(), histogramPaint)
    }

    override fun drawText(
        canvas: Canvas,
        view: BaseKChartView,
        x: Float,
        y: Float,
        point: IKLine?
    ) {
        point ?: return
        var tempX = x
        val lineConfigs = getConfig().maConfig

        val volFormat = getValueFormatter().format(point.getVolume())
        val volStr = "Vol: $volFormat  "
        val textPaint = view.legendTextPaint()
        textPaint.color = getConfig().volTextColor
        canvas.drawText(volStr, tempX, y, textPaint)
        tempX += textPaint.measureText(volStr)

        for (lineConfig in lineConfigs) {
            val childIndex = point.getChildIndex(ChartType.VOL, lineConfig.id)?:continue
            if (childIndex.isExtremum) {
                continue
            }
            val maFormat = getValueFormatter().format(childIndex.toString())
            val maStr = "${lineConfig.name}(${lineConfig.cycle}): $maFormat  "
            textPaint.color = lineConfig.color
            canvas.drawText(maStr, tempX, y, textPaint)
            tempX += textPaint.measureText(maStr)
        }


    }

    override fun getMaxValue(point: IKLine?): Double {
        val volume = point?.getVolume()?.toDouble() ?: Double.MIN_VALUE
        val indexList = point?.getChildIndexList(ChartType.VOL)
        val maxIndex = indexList?.values?.maxOrNull()
        return if (maxIndex == null || maxIndex.isExtremum) {
            volume
        } else {
            max(volume, maxIndex)
        }
    }

    override fun getMinValue(point: IKLine?): Double {
        val volume = point?.getVolume()?.toDouble() ?: Double.MAX_VALUE
        val indexList = point?.getChildIndexList(ChartType.VOL)
        val minIndex = indexList?.values?.minOrNull()
        return if (minIndex == null || minIndex.isExtremum) {
            volume
        } else {
            min(volume, minIndex)
        }
    }

    override fun calculate(index: Int, curPoint: IKLine, list: List<IKLine>) {
        for (lineConfig in getConfig().maConfig) {
            val id = lineConfig.id
            val cycle = lineConfig.cycle
            val maValue = if (cycle >= index) {
                Double.NaN
            } else {
                list
                    .subList(index - cycle,index)
                    .sumOf { it.getVolume().toDouble() } / cycle
            }
            curPoint.setChildIndex(ChartType.VOL, id, maValue)
        }
    }

    override fun getDefaultConfig() = VolConfig(
        name = "Vol",
        maConfig = listOf(LineConfig("MA",1,5,1f.dp,Color.BLUE,true)),
        volTextColor = Color.MAGENTA,
        upIsFill = true,
        downIsFIll = true
    )

}
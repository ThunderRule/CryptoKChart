package top.zdever.kline.draw

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import top.zdever.kline.base.BaseKChartView
import top.zdever.kline.base.IChartDraw
import top.zdever.kline.config.KdjConfig
import top.zdever.kline.config.LineConfig
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
class KDJChart(override val name: String) : IChartDraw<KdjConfig>() {
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
        val kConfig = getConfig().kConfig
        if (kConfig.isOpen) {
            val preK = prePoint?.getChildIndex(ChartType.KDJ, 1)
            val curK = curPoint?.getChildIndex(ChartType.KDJ, 1)
            if (!preK.isExtremum && !curK.isExtremum) {
                linePaint.setColor(kConfig.color)
                linePaint.strokeWidth = kConfig.width
                canvas.drawLine(
                    preX,
                    view.valueToY(ChartType.KDJ, preK!!.toFloat()),
                    curX,
                    view.valueToY(ChartType.KDJ, curK!!.toFloat()),
                    linePaint
                )
            }
        }

        val dConfig = getConfig().dConfig
        if (dConfig.isOpen) {
            val preD = prePoint?.getChildIndex(ChartType.KDJ, 2)
            val curD = curPoint?.getChildIndex(ChartType.KDJ, 2)
            if (!preD.isExtremum && !curD.isExtremum) {
                linePaint.setColor(dConfig.color)
                linePaint.strokeWidth = dConfig.width
                canvas.drawLine(
                    preX,
                    view.valueToY(ChartType.KDJ, preD!!.toFloat()),
                    curX,
                    view.valueToY(ChartType.KDJ, curD!!.toFloat()),
                    linePaint
                )
            }
        }

        val jConfig = getConfig().jConfig
        if (jConfig.isOpen) {
            val preJ = prePoint?.getChildIndex(ChartType.KDJ, 3)
            val curJ = curPoint?.getChildIndex(ChartType.KDJ, 3)
            if (!preJ.isExtremum && !curJ.isExtremum) {
                linePaint.setColor(jConfig.color)
                linePaint.strokeWidth = jConfig.width
                canvas.drawLine(
                    preX,
                    view.valueToY(ChartType.KDJ, preJ!!.toFloat()),
                    curX,
                    view.valueToY(ChartType.KDJ, curJ!!.toFloat()),
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

        val kConfig = getConfig().kConfig
        if (kConfig.isOpen){
            val formatK = getValueFormatter().format(point.getChildIndex(name, 1)?.toString())
            val kStr = "K:${formatK} "
            textPaint.color = kConfig.color
            canvas.drawText(kStr,tempX,y,textPaint)
            tempX += textPaint.measureText(kStr)
        }

        val dConfig = getConfig().dConfig
        if (dConfig.isOpen){
            val formatD = getValueFormatter().format(point.getChildIndex(name, 2)?.toString())
            val dStr = "D:${formatD} "
            textPaint.color = dConfig.color
            canvas.drawText(dStr,tempX,y,textPaint)
            tempX += textPaint.measureText(dStr)
        }

        val jConfig = getConfig().jConfig
        if (jConfig.isOpen){
            val formatJ = getValueFormatter().format(point.getChildIndex(name, 3)?.toString())
            val jStr = "J:${formatJ}"
            textPaint.color = jConfig.color
            canvas.drawText(jStr,tempX,y,textPaint)
        }
    }

    override fun getMaxValue(point: IKLine?): Double {
        point ?: return Double.MIN_VALUE
        val max = point.getChildIndexList(ChartType.KDJ)?.values?.maxOrNull()
        return if (max.isExtremum) {
            Double.MIN_VALUE
        } else {
            max!!
        }
    }

    override fun getMinValue(point: IKLine?): Double {
        point ?: return Double.MAX_VALUE
        val min = point.getChildIndexList(ChartType.KDJ)?.values?.minOrNull()
        return if (min.isExtremum) {
            Double.MAX_VALUE
        } else {
            min!!
        }
    }

    override fun getDefaultConfig() = KdjConfig(
        name = ChartType.KDJ,
        cycle = 9,
        moveAvgCycle1 = 3,
        moveAvgCycle2 = 3,
        kConfig = LineConfig("K", 1, 1, 1f.dp, Color.RED, true),
        dConfig = LineConfig("D", 2, 1, 1f.dp, Color.BLUE, true),
        jConfig = LineConfig("J", 3, 1, 1f.dp, Color.GREEN, true),
    )

    private var k: Double? = null
    private var d: Double? = null
    override fun calculate(index: Int, curPoint: IKLine, list: List<IKLine>) {
        val kPeriod = getConfig().cycle
        val dPeriod = getConfig().moveAvgCycle1
        val jPeriod = getConfig().moveAvgCycle2
        if (list.size >= kPeriod) {
            if (index >= kPeriod - 1) {
                val low = list.subList(index - kPeriod + 1, index + 1)
                    .minOf { it.getLowPrice().toDouble() }
                val high = list.subList(index - kPeriod + 1, index + 1)
                    .maxOf { it.getHighPrice().toDouble() }
                val close = curPoint.getClosePrice().toDouble()

                val slowing = 3.0
                val rsv = if (high == low) 100.0 else ((close - low) / (high - low)) * 100.0
                k = if (k == null) rsv else (1.0 / slowing) * rsv + ((slowing - 1.0) / slowing) * k!!
                d = if (d == null) k!! else (1.0 / dPeriod) * k!! + ((dPeriod - 1.0) / dPeriod) * d!!
                val j = jPeriod * k!! - (jPeriod - 1.0) * d!!
                curPoint.setChildIndex(ChartType.KDJ, 1, k!!)
                curPoint.setChildIndex(ChartType.KDJ, 2, d!!)
                curPoint.setChildIndex(ChartType.KDJ, 3, j)
            }
        }
    }
}
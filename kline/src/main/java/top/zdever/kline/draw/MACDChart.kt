package top.zdever.kline.draw

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import top.zdever.kline.base.BaseKChartView
import top.zdever.kline.base.IChartDraw
import top.zdever.kline.config.LineConfig
import top.zdever.kline.config.MacdConfig
import top.zdever.kline.constants.ChartType
import top.zdever.kline.model.IKLine
import top.zdever.kline.utils.dp
import top.zdever.kline.utils.isExtremum

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/11
 */
class MACDChart(override val name: String) : IChartDraw<MacdConfig>() {

    private val redPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val redStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val greenStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        redStrokePaint.style = Paint.Style.STROKE
        greenStrokePaint.style = Paint.Style.STROKE
        redStrokePaint.strokeWidth = 1f.dp
        greenStrokePaint.strokeWidth = 1f.dp
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
        val redColor = getConfig().downColor
        val greenColor = getConfig().upColor
        redPaint.color = redColor
        redStrokePaint.color = redColor
        greenPaint.color = greenColor
        greenStrokePaint.color = greenColor

        val longIncreaseFill = getConfig().longIncreaseFill
        val longDecreaseFill = getConfig().longDecreaseFill
        val shortIncreaseFill = getConfig().shortIncreaseFill
        val shortDecreaseFill = getConfig().shortDecreaseFill
        val preMacdValue = prePoint?.getChildIndex(ChartType.MACD, 1) ?: return
        val curMacdValue = curPoint?.getChildIndex(ChartType.MACD, 1) ?: return
        if (curMacdValue >= 0) {
            if (preMacdValue < curMacdValue) {
                if (longIncreaseFill) {
                    drawMacd(canvas, curX, curMacdValue, redPaint, greenPaint, view)
                } else {
                    drawMacd(canvas, curX, curMacdValue, redStrokePaint, greenStrokePaint, view)
                }
            } else {
                if (longDecreaseFill) {
                    drawMacd(canvas, curX, curMacdValue, redPaint, greenPaint, view)
                } else {
                    drawMacd(canvas, curX, curMacdValue, redStrokePaint, greenStrokePaint, view)
                }
            }
        } else {
            if (preMacdValue < curMacdValue) {
                if (shortIncreaseFill) {
                    drawMacd(canvas, curX, curMacdValue, redPaint, greenPaint, view)
                } else {
                    drawMacd(canvas, curX, curMacdValue, redStrokePaint, greenStrokePaint, view)
                }
            } else {
                if (shortDecreaseFill) {
                    drawMacd(canvas, curX, curMacdValue, redPaint, greenPaint, view)
                } else {
                    drawMacd(canvas, curX, curMacdValue, redStrokePaint, greenStrokePaint, view)
                }
            }
        }

        val difConfig = getConfig().difConfig
        if (difConfig.isOpen) {
            val preDif = prePoint.getChildIndex(ChartType.MACD, 2)
            val curDif = curPoint.getChildIndex(ChartType.MACD, 2)
            if (!preDif.isExtremum && !curDif.isExtremum) {
                linePaint.setColor(difConfig.color)
                linePaint.strokeWidth = difConfig.width
                canvas.drawLine(
                    preX,
                    view.valueToY(ChartType.MACD, preDif!!.toFloat()),
                    curX,
                    view.valueToY(ChartType.MACD, curDif!!.toFloat()),
                    linePaint
                )
            }
        }
        val deaConfig = getConfig().deaConfig
        if (deaConfig.isOpen) {
            val preDea = prePoint.getChildIndex(ChartType.MACD, 3)
            val curDea = curPoint.getChildIndex(ChartType.MACD, 3)
            if (!preDea.isExtremum && !curDea.isExtremum) {
                linePaint.setColor(deaConfig.color)
                linePaint.strokeWidth = deaConfig.width
                canvas.drawLine(
                    preX,
                    view.valueToY(ChartType.MACD, preDea!!.toFloat()),
                    curX,
                    view.valueToY(ChartType.MACD, curDea!!.toFloat()),
                    linePaint
                )
            }
        }
    }

    private fun drawMacd(
        canvas: Canvas,
        x: Float,
        value: Double,
        redPaint: Paint,
        greenPaint: Paint,
        view: BaseKChartView
    ) {
        val halfW = (view.mPointWidth - 4) / 2 * view.scaleX
        if (value >= 0) {
            canvas.drawRect(
                x - halfW,
                view.valueToY(ChartType.MACD, value.toFloat()),
                x + halfW,
                view.valueToY(ChartType.MACD, 0f),
                redPaint
            )
        } else {
            canvas.drawRect(
                x - halfW,
                view.valueToY(ChartType.MACD, 0f),
                x + halfW,
                view.valueToY(ChartType.MACD, value.toFloat()),
                greenPaint
            )
        }
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
        val textPaint = view.legendTextPaint()

        val difConfig = getConfig().difConfig
        if (difConfig.isOpen){
            val difStr = "DIF:${getValueFormatter().format(point.getChildIndex(name,difConfig.id)?.toString())} "
            textPaint.color = difConfig.color
            canvas.drawText(difStr,tempX,y,textPaint)
            tempX += textPaint.measureText(difStr)
        }

        val deaConfig = getConfig().deaConfig
        if (deaConfig.isOpen){
            val deaStr = "DEA:${getValueFormatter().format(point.getChildIndex(name,deaConfig.id)?.toString())} "
            textPaint.color = deaConfig.color
            canvas.drawText(deaStr,tempX,y,textPaint)
            tempX += textPaint.measureText(deaStr)
        }

        val macdStr = "MACD:${getValueFormatter().format(point.getChildIndex(name,1)?.toString())}"
        textPaint.color = getConfig().legendColor
        canvas.drawText(macdStr,tempX,y,textPaint)


    }

    override fun getMaxValue(point: IKLine?): Double {
        point ?: return Double.MIN_VALUE
        val max = point.getChildIndexList(ChartType.MACD)?.values?.maxOrNull()
        return if (max == null || max.isExtremum) {
            Double.MIN_VALUE
        } else {
            max
        }
    }

    override fun getMinValue(point: IKLine?): Double {
        point ?: return Double.MAX_VALUE
        val min = point.getChildIndexList(ChartType.MACD)?.values?.minOrNull()
        return if (min == null || min.isExtremum) {
            Double.MAX_VALUE
        } else {
            min
        }
    }

    private var fastEMA: Double? = null
    private var slowEMA: Double? = null
    private var signalEMA: Double? = null
    private val difBuffer = mutableListOf<Double>()
    override fun calculate(index: Int, curPoint: IKLine, list: List<IKLine>) {
        val fastPeriod = getConfig().shortCycle
        val slowPeriod = getConfig().longCycle
        val singlePeriod = getConfig().moveAvgCycle

        val fastMultiplier = 2f / (fastPeriod + 1)
        val slowMultiplier = 2f / (slowPeriod + 1)
        val singleMultiplier = 2f / (singlePeriod + 1)
        val curClose = curPoint.getClosePrice().toDouble()

        if (index >= fastPeriod - 1) {
            if (fastEMA == null) {
                fastEMA = list.subList(index - fastPeriod + 1, index + 1)
                    .map { it.getClosePrice().toDouble() }
                    .average()
            } else {
                fastEMA = (curClose - fastEMA!!) * fastMultiplier + fastEMA!!
            }
        }
        if (index >= slowPeriod - 1) {
            if (slowEMA == null) {
                slowEMA = list.subList(index - slowPeriod + 1, index + 1)
                    .map { it.getClosePrice().toDouble() }
                    .average()
            } else {
                slowEMA = (curClose - slowEMA!!) * slowMultiplier + slowEMA!!
            }

            val dif = fastEMA!!- slowEMA!!
            difBuffer.add(dif)
            if (difBuffer.size >= singlePeriod){
                if (signalEMA == null){
                    signalEMA = difBuffer.takeLast(singlePeriod).average()
                }else{
                    signalEMA = (dif - signalEMA!!) * singleMultiplier + signalEMA!!
                }
                val macd = (dif - signalEMA!!) * 2

                curPoint.setChildIndex(ChartType.MACD,1,macd)
                curPoint.setChildIndex(ChartType.MACD,2,dif)
                curPoint.setChildIndex(ChartType.MACD,3,signalEMA!!)
            }

        }
    }

    override fun getDefaultConfig() = MacdConfig(
        name = ChartType.MACD,
        shortCycle = 12,
        longCycle = 16,
        moveAvgCycle = 9,
        difConfig = LineConfig("DIF", 2, 1, 1f.dp, Color.BLUE, true),
        deaConfig = LineConfig("DEA", 3, 1, 1f.dp, Color.MAGENTA, true),
        upColor = Color.GREEN,
        downColor = Color.RED,
        legendColor = Color.BLUE,
        longIncreaseFill = true,
        longDecreaseFill = false,
        shortIncreaseFill = true,
        shortDecreaseFill = false
    )

}
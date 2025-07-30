package top.zdever.kline.draw

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import top.zdever.kline.base.BaseKChartView
import top.zdever.kline.base.IChartDraw
import top.zdever.kline.config.LineConfig
import top.zdever.kline.config.MainConfig
import top.zdever.kline.constants.CandleStyle
import top.zdever.kline.constants.ChartType
import top.zdever.kline.constants.IndexType
import top.zdever.kline.model.IKLine
import top.zdever.kline.utils.dp
import top.zdever.kline.utils.isExtremum
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/11
 */
class MainChart(override val name: String) : IChartDraw<MainConfig>() {
    private val mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mUpPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mUpHollowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mDownPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mDownHollowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        mUpPaint.color = Color.GREEN
        mUpHollowPaint.color = Color.GREEN
        mUpHollowPaint.style = Paint.Style.STROKE
        mDownPaint.color = Color.RED
        mDownHollowPaint.color = Color.RED
        mDownHollowPaint.style = Paint.Style.STROKE
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

        if (curPoint != null) {
            when (getConfig().candleStyle) {
                CandleStyle.STYLE_USA -> drawUsaCandle(
                    canvas,
                    curX,
                    curPoint.getHighPrice().toFloat(),
                    curPoint.getLowPrice().toFloat(),
                    curPoint.getOpenPrice().toFloat(),
                    curPoint.getClosePrice().toFloat(),
                    view
                )

                CandleStyle.STYLE_FILL -> drawCandle(
                    canvas,
                    curX,
                    curPoint.getHighPrice().toFloat(),
                    curPoint.getLowPrice().toFloat(),
                    curPoint.getOpenPrice().toFloat(),
                    curPoint.getClosePrice().toFloat(),
                    view
                )

                CandleStyle.STYLE_HOLLOW -> drawHollowCandle(
                    canvas,
                    curX,
                    curPoint.getHighPrice().toFloat(),
                    curPoint.getLowPrice().toFloat(),
                    curPoint.getOpenPrice().toFloat(),
                    curPoint.getClosePrice().toFloat(),
                    view
                )

                CandleStyle.STYLE_HISTOGRAM -> TODO()
                CandleStyle.STYLE_LINE -> TODO()
                CandleStyle.STYLE_LINE_FILL -> TODO()
                CandleStyle.STYLE_LINE_BASELINE -> TODO()
                CandleStyle.STYLE_AVG -> TODO()
            }
        }

        when (val indexType = getConfig().indicator) {
            IndexType.MA -> {
                if (curPoint != null && prePoint != null) {
                    val mainMaList = getConfig().maConfig
                    for (lineConfig in mainMaList) {
                        val preValue = prePoint.getMainIndex(indexType, lineConfig.id) ?: return
                        val curValue = curPoint.getMainIndex(indexType, lineConfig.id) ?: return
                        if (preValue.isExtremum || curValue.isExtremum) {
                            continue
                        }
                        mLinePaint.color = lineConfig.color
                        mLinePaint.strokeWidth = lineConfig.width
                        canvas.drawLine(
                            preX,
                            view.valueToY(ChartType.MAIN, preValue.toFloat()),
                            curX,
                            view.valueToY(ChartType.MAIN, curValue.toFloat()),
                            mLinePaint
                        )
                    }
                }
            }

            IndexType.EMA -> {
                if (curPoint != null && prePoint != null) {
                    val mainEmaList = getConfig().emaConfig
                    for (lineConfig in mainEmaList) {
                        val preValue = prePoint.getMainIndex(indexType, lineConfig.id) ?: return
                        val curValue = curPoint.getMainIndex(indexType, lineConfig.id) ?: return
                        if (preValue.isExtremum || curValue.isExtremum) {
                            continue
                        }
                        mLinePaint.color = lineConfig.color
                        mLinePaint.strokeWidth = lineConfig.width
                        canvas.drawLine(
                            preX,
                            view.valueToY(ChartType.MAIN, preValue.toFloat()),
                            curX,
                            view.valueToY(ChartType.MAIN, curValue.toFloat()),
                            mLinePaint
                        )
                    }
                }
            }

            IndexType.BOLL -> {
                if (curPoint != null && prePoint != null) {
                    val mainBollConfig = getConfig().bollConfig
                    val upLineConfig = mainBollConfig.upConfig
                    val mbLineConfig = mainBollConfig.mbConfig
                    val dnLineConfig = mainBollConfig.dnConfig

                    val preUpValue = prePoint.getMainIndex(indexType, upLineConfig.id) ?: return
                    val curUpValue = curPoint.getMainIndex(indexType, upLineConfig.id) ?: return
                    if (!preUpValue.isExtremum && !curUpValue.isExtremum) {
                        mLinePaint.color = upLineConfig.color
                        mLinePaint.strokeWidth = upLineConfig.width
                        canvas.drawLine(
                            preX,
                            view.valueToY(ChartType.MAIN, preUpValue.toFloat()),
                            curX,
                            view.valueToY(ChartType.MAIN, curUpValue.toFloat()),
                            mLinePaint
                        )
                    }
                    val preMbValue = prePoint.getMainIndex(indexType, mbLineConfig.id) ?: return
                    val curMbValue = curPoint.getMainIndex(indexType, mbLineConfig.id) ?: return
                    if (!preMbValue.isExtremum && !curMbValue.isExtremum) {
                        mLinePaint.color = mbLineConfig.color
                        mLinePaint.strokeWidth = mbLineConfig.width
                        canvas.drawLine(
                            preX,
                            view.valueToY(ChartType.MAIN, preMbValue.toFloat()),
                            curX,
                            view.valueToY(ChartType.MAIN, curMbValue.toFloat()),
                            mLinePaint
                        )
                    }
                    val preDnValue = prePoint.getMainIndex(indexType, dnLineConfig.id) ?: return
                    val curDnValue = curPoint.getMainIndex(indexType, dnLineConfig.id) ?: return
                    if (!preDnValue.isExtremum && !curDnValue.isExtremum) {
                        mLinePaint.color = dnLineConfig.color
                        mLinePaint.strokeWidth = dnLineConfig.width
                        canvas.drawLine(
                            preX,
                            view.valueToY(ChartType.MAIN, preDnValue.toFloat()),
                            curX,
                            view.valueToY(ChartType.MAIN, curDnValue.toFloat()),
                            mLinePaint
                        )
                    }

                }
            }

            IndexType.SAR -> {

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
        val hY = view.valueToY(ChartType.MAIN, h)
        val lY = view.valueToY(ChartType.MAIN, l)
        val oY = view.valueToY(ChartType.MAIN, o)
        val cY = view.valueToY(ChartType.MAIN, c)
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

    private fun drawUsaCandle(
        canvas: Canvas,
        x: Float,
        h: Float,
        l: Float,
        o: Float,
        c: Float,
        view: BaseKChartView
    ) {
        val hY = view.valueToY(ChartType.MAIN, h)
        val lY = view.valueToY(ChartType.MAIN, l)
        val oY = view.valueToY(ChartType.MAIN, o)
        val cY = view.valueToY(ChartType.MAIN, c)
        val width = (view.mPointWidth - 4) / 2 * view.scaleX
        if (oY < cY) {
            canvas.drawLine(x, hY, x, lY, mDownPaint)
            canvas.drawLine(x - width, oY, x, oY, mDownPaint)
            canvas.drawLine(x, cY, x + width, cY, mDownPaint)
        } else if (oY > cY) {
            canvas.drawLine(x, hY, x, lY, mUpPaint)
            canvas.drawLine(x - width, oY, x, oY, mUpPaint)
            canvas.drawLine(x, cY, x + width, cY, mUpPaint)
        } else {
            canvas.drawLine(x, hY, x, lY - 1, mUpPaint)
            canvas.drawLine(x - width, hY, x + width, lY, mUpPaint)
        }
    }

    private fun drawHollowCandle(
        canvas: Canvas,
        x: Float,
        h: Float,
        l: Float,
        o: Float,
        c: Float,
        view: BaseKChartView
    ) {
        val hY = view.valueToY(ChartType.MAIN, h)
        val lY = view.valueToY(ChartType.MAIN, l)
        val oY = view.valueToY(ChartType.MAIN, o)
        val cY = view.valueToY(ChartType.MAIN, c)
        val width = (view.mPointWidth - 4) / 2 * view.scaleX
        if (oY < cY) {
            drawOneCandle(canvas, x, hY, lY, oY, cY, x - width, x + width, mDownHollowPaint)
        } else if (oY > cY) {
            drawOneCandle(canvas, x, hY, lY, cY, oY, x - width, x + width, mUpHollowPaint)
        } else {
            drawOneCandle(canvas, x, hY, lY, cY - 1, oY, x - width, x + width, mUpHollowPaint)
        }
    }

    override fun drawText(
        canvas: Canvas,
        view: BaseKChartView,
        x: Float,
        y: Float,
        point: IKLine?
    ) {
        val textPaint = view.legendTextPaint()
        when (getConfig().indicator) {
            IndexType.MA -> {
                val mainMaList = getConfig().maConfig
                var tempX = x

                for (lineConfig in mainMaList) {
                    val maValue =
                        point?.getMainIndex(IndexType.MA, lineConfig.id) ?: Double.MIN_VALUE
                    if (maValue.isExtremum) {
                        continue
                    }
                    val textStr = StringBuilder()
                    val format = getValueFormatter().format(maValue.toString())
                    textStr.append(lineConfig.name).append(lineConfig.cycle).append(":")
                        .append(format)
                        .append(" ")
                    textPaint.color = lineConfig.color
                    canvas.drawText(textStr.toString(), tempX, y, textPaint)
                    tempX += textPaint.measureText(textStr.toString())
                }
            }

            IndexType.EMA -> {
                val mainEmaList = getConfig().emaConfig
                var tempX = x

                for (lineConfig in mainEmaList) {
                    val emaValue =
                        point?.getMainIndex(IndexType.EMA, lineConfig.id) ?: Double.MIN_VALUE
                    if (emaValue.isExtremum) {
                        continue
                    }
                    val textStr = StringBuilder()
                    val format = getValueFormatter().format(emaValue.toString())
                    textStr.append(lineConfig.name).append(lineConfig.cycle).append(":")
                        .append(format)
                        .append(" ")
                    textPaint.color = lineConfig.color
                    canvas.drawText(textStr.toString(), tempX, y, textPaint)
                    tempX += textPaint.measureText(textStr.toString())
                }
            }

            IndexType.BOLL -> {
                var tempX = x
                val bollConfig = getConfig().bollConfig
                val textBuilder = StringBuilder()
                textBuilder.append("BOLL:(").append(bollConfig.cycle).append(",").append(bollConfig.bandWidth).append(") ")
                textPaint.color = bollConfig.legendColor
                canvas.drawText(textBuilder.toString(),tempX,y,textPaint)
                tempX += textPaint.measureText(textBuilder.toString())
                val bollUp = bollConfig.upConfig
                if (bollUp.isOpen){
                    val upValue = point?.getMainIndex(IndexType.BOLL, bollUp.id)
                    if (!upValue.isExtremum){
                        val builder = StringBuilder()
                        builder.append("UP:").append(getValueFormatter().format(upValue.toString()))
                        textPaint.color = bollUp.color
                        canvas.drawText(builder.toString(),tempX,y,textPaint)
                        tempX += textPaint.measureText(builder.toString())
                    }
                }
                val bollMb = bollConfig.mbConfig
                if (bollMb.isOpen){
                    val mbValue = point?.getMainIndex(IndexType.BOLL, bollMb.id)
                    if (!mbValue.isExtremum){
                        val builder = StringBuilder()
                        builder.append(" MB:").append(getValueFormatter().format(mbValue.toString()))
                        textPaint.color = bollMb.color
                        canvas.drawText(builder.toString(),tempX,y,textPaint)
                        tempX += textPaint.measureText(builder.toString())
                    }
                }
                val bollDn = bollConfig.dnConfig
                if (bollDn.isOpen){
                    val dnValue = point?.getMainIndex(IndexType.BOLL, bollDn.id)
                    if (!dnValue.isExtremum){
                        val builder = StringBuilder()
                        builder.append(" DN:").append(getValueFormatter().format(dnValue.toString()))
                        textPaint.color = bollDn.color
                        canvas.drawText(builder.toString(),tempX,y,textPaint)
                    }
                }
            }
        }
    }

    override fun getMaxValue(point: IKLine?): Double {
        val highPrice = point?.getHighPrice()?.toDouble() ?: Double.MIN_VALUE
        val indexType = getConfig().indicator
        val indexList = point?.getMainIndexList(indexType)
        val maxIndex = indexList?.values?.maxOrNull()
        return if (maxIndex == null || maxIndex.isExtremum) {
            highPrice
        } else {
            max(highPrice, maxIndex)
        }
    }

    override fun getMinValue(point: IKLine?): Double {
        val lowPrice = point?.getLowPrice()?.toDouble() ?: Double.MAX_VALUE
        val indexType = getConfig().indicator
        val indexList = point?.getMainIndexList(indexType)
        val minIndex = indexList?.values?.minOrNull()
        return if (minIndex == null || minIndex.isExtremum) {
            lowPrice
        } else {
            min(lowPrice, minIndex)
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
                    .sumOf { it.getClosePrice().toDouble() } / cycle
            }
            curPoint.setMainIndex(IndexType.MA, id, maValue)
        }

        for (lineConfig in getConfig().emaConfig) {
            val id = lineConfig.id
            val cycle = lineConfig.cycle
            val emaValue = if (cycle > index) {
                Double.NaN
            } else if (cycle == index){
                list.take(cycle).map {
                    it.getClosePrice().toDouble()
                }.average()
            }else{
                val currentClose = curPoint.getClosePrice().toDouble()
                val preEma = list[index-1].getMainIndex(IndexType.EMA,id)?:0.0
                (currentClose - preEma) * (2f/(cycle+1))+preEma
            }
            curPoint.setMainIndex(IndexType.EMA,id,emaValue)
        }

        val bollConfig = getConfig().bollConfig
        val bollDn = bollConfig.dnConfig
        val bollMb = bollConfig.mbConfig
        val bollUp = bollConfig.upConfig
        if (bollConfig.cycle > index){
            curPoint.setMainIndex(IndexType.BOLL,bollUp.id, Double.NaN)
            curPoint.setMainIndex(IndexType.BOLL,bollMb.id, Double.NaN)
            curPoint.setMainIndex(IndexType.BOLL,bollDn.id, Double.NaN)
        }else{
            val window = list.subList(index - bollConfig.cycle + 1, index + 1)
            val values = window.map { it.getClosePrice().toDouble() }
            val midBand = values.average()
            curPoint.setMainIndex(IndexType.BOLL,bollMb.id, midBand)
            val variance = values.map { (it - midBand).pow(2) }.average()
            val standardDeviation = sqrt(variance)
            curPoint.setMainIndex(IndexType.BOLL,bollUp.id,midBand+bollConfig.bandWidth * standardDeviation)
            curPoint.setMainIndex(IndexType.BOLL,bollDn.id,midBand-bollConfig.bandWidth * standardDeviation)
        }

        val sarConfig = getConfig().sarConfig
        if (list.size<2){
            curPoint.setMainIndex(IndexType.SAR,1,Double.MIN_VALUE)
        }else {
            val first = list[0]
            val second = list[1]
            val isBullish = second.getClosePrice().toFloat() > first.getClosePrice().toFloat()

            if (index == 0){
                curPoint.setMainIndex(IndexType.SAR,1,curPoint.getClosePrice().toDouble())
            }else{
            }
        }
    }

    override fun getDefaultConfig() = MainConfig(
        name = "Main",
        maConfig = listOf(
            LineConfig("MA",1, 7, 1f.dp, Color.RED, true),
            LineConfig("MA",2, 25, 1f.dp, Color.GREEN, true),
            LineConfig("MA",3, 99, 1f.dp, Color.BLUE, true),
        ),
        emaConfig = listOf(
            LineConfig("EMA",1, 7, 1f.dp, Color.RED, true),
            LineConfig("EMA",2, 15, 1f.dp, Color.GREEN, true),
            LineConfig("EMA",3, 99, 1f.dp, Color.BLUE, true),
        ),
        bollConfig = MainConfig.BollConfig(
            cycle = 21,
            bandWidth = 2.0f,
            legendColor = Color.YELLOW,
            upConfig = LineConfig("UP",1,1,1f.dp,Color.RED,true),
            mbConfig = LineConfig("MB",2,1,1f.dp,Color.GREEN,true),
            dnConfig = LineConfig("DN",3,1,1f.dp,Color.BLUE,true),
        ),
        sarConfig = MainConfig.SarConfig(
            start = 0.02f,
            max = 0.2f,
            color = Color.RED
        )
    )

}
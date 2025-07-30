package top.zdever.kline.base

import android.animation.ValueAnimator
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import top.zdever.kline.R
import top.zdever.kline.calculate.CalculateManager
import top.zdever.kline.config.ChartConfig
import top.zdever.kline.config.ConfigManger
import top.zdever.kline.constants.CandleStyle
import top.zdever.kline.constants.ChartType
import top.zdever.kline.constants.IconPosition
import top.zdever.kline.constants.MAIN_CANDLE
import top.zdever.kline.constants.TIME_15M
import top.zdever.kline.constants.TIME_LINE
import top.zdever.kline.constants.TimeType
import top.zdever.kline.draw.KDJChart
import top.zdever.kline.draw.MACDChart
import top.zdever.kline.draw.MainChart
import top.zdever.kline.draw.VolumeChart
import top.zdever.kline.draw.WRChart
import top.zdever.kline.format.DefaultDateFormat
import top.zdever.kline.format.DefaultValueFormat
import top.zdever.kline.format.IValueFormat
import top.zdever.kline.utils.CountDownUtils
import top.zdever.kline.utils.SavedUtils
import top.zdever.kline.utils.dp
import top.zdever.kline.utils.logd
import kotlin.math.max
import kotlin.math.min

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/10
 */
open class BaseKChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ScrollAndScaleView(context, attrs, defStyle) {

    private val DEFAULT_MAIN_HEIGHT = 300.dp
    private val DEFAULT_SUB_HEIGHT = 80.dp

    private val DEFAULT_LEGEND_MARGIN = 10.dp

    protected var gridColumns = 4
    protected var gridRows = 4
    protected var logoIcon: Bitmap? = null
    protected var fullScreenIcon: Bitmap? = null
    protected var mBaseLine = 0f
    protected var mBorderRadius = 4f.dp
    protected var mAxisPaddingH = 4f.dp
    protected var mCandleStyle = CandleStyle.STYLE_USA
    protected val painXAxis = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val painYAxis = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val mTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    protected val mLegendTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    protected val mBorderTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    protected val mAxisTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    protected val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val selectCrossPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val mBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val mBorderBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected var popBgColor = Color.WHITE
    protected var crossBgColor = Color.BLACK

    private val mCountDown = CountDownUtils()
    private var chartMap = mutableMapOf<String, IChartDraw<*>>()
    private var mMainHeight = DEFAULT_MAIN_HEIGHT
    private var mChildHeight = DEFAULT_SUB_HEIGHT
    private var mLegendMargin = DEFAULT_LEGEND_MARGIN
    private var viewWidth = 0f
    private var viewHeight = 0f
    private var columSpace = 0f
    private var rowSpace = 0f
    private var logoTop = 0f
    private var fullScreenTop = 0f
    private var logoPosition = IconPosition.BOTTOM_LEFT
    private var mStartIndex = 0
    private var mEndIndex = 0
    private var mTime = TIME_15M
    private var mainLineType = MAIN_CANDLE
    private var mainYScale = 1f
    private var mDateFormat = DefaultDateFormat()
    private var mValueFormat: IValueFormat = DefaultValueFormat()
    private var selectedX = 0f
    private var selectedY = 0f
    private var selectedPop = mutableMapOf<String, String>()
    private var mMaxYLabelWidth = 0f
    private var countDownStr = "--"
    private var timeUnit = 900000L

    private var invalidateTime = 0L
    private var mConfig = ConfigManger.getConfig()
    private val priceInLineRect = RectF()

    @TimeType
    var selectedTime: Long = TIME_15M

    private val dataSetObserver = object : DataSetObserver() {
        override fun onChanged() {
            mItemCount = mAdapter?.getCount() ?: 0
            changeTranslate(viewWidth - getDataWidth())
            invalidate()
        }

        override fun onInvalidated() {
            mItemCount = mAdapter?.getCount() ?: 0
        }
    }

    init {
        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.strokeWidth = 0.5f.dp
        val dashPathEffect = DashPathEffect(floatArrayOf(3f.dp, 3f.dp), 0f)
        selectCrossPaint.setPathEffect(dashPathEffect)
        selectCrossPaint.strokeWidth = 1f.dp

        for (index in SavedUtils.getIndexes()) {
            val chart = when (index) {
                ChartType.MACD -> {
                    MACDChart(index)
                }

                ChartType.VOL -> {
                    VolumeChart(index)
                }

                ChartType.KDJ -> {
                    KDJChart(index)
                }

                ChartType.WR -> {
                    WRChart(index)
                }

                else -> {
                    MainChart(index)
                }
            }
            chartMap[index] = chart
            ConfigManger.setChildDefault(index, chart.getDefaultConfig())
            CalculateManager.register(chart)
            maxMinValueMap[index] = doubleArrayOf(Double.MIN_VALUE, Double.MAX_VALUE, 0.0)
        }

        mCountDown.setOnTick {
            formatCountdown(it)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val totalHeight = if (chartMap.keys.contains(ChartType.MAIN)) {
            mMainHeight + (chartMap.size - 1) * mChildHeight + mTextHeight
        } else {
            chartMap.size * mChildHeight + mTextHeight
        }
        setMeasuredDimension(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(totalHeight.toInt(), MeasureSpec.AT_MOST)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w.toFloat()
        viewHeight = h.toFloat()

        initRect()
    }

    private fun initRect() {
        columSpace = viewWidth / gridColumns
        var lastPositionY = 0
        for (childName in chartMap.keys) {
            val chartHeight = if (childName == ChartType.MAIN) {
                rowSpace = (mMainHeight - mTextHeight) / gridRows
                mMainHeight
            } else {
                mChildHeight
            }
            val tempBottom = if (xBottomIndex == childName) {
                lastPositionY + chartHeight + mTextHeight.toInt()
            } else {
                lastPositionY + chartHeight
            }
            childRectMap[childName] = Rect(0, lastPositionY, viewWidth.toInt(), tempBottom)
            lastPositionY = tempBottom
        }


        if (logoIcon != null) {
            when (logoPosition) {
                IconPosition.TOP_LEFT -> TODO()
                IconPosition.TOP_RIGHT -> TODO()
                IconPosition.BOTTOM_LEFT -> TODO()
                IconPosition.BOTTOM_RIGHT -> TODO()
                IconPosition.CENTER -> TODO()
                IconPosition.FREE -> TODO()
            }
        }

        if (fullScreenIcon != null) {
            TODO()
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawGrid(canvas)
        drawLogo(canvas)
        if (viewWidth != 0f) {
            calculateValues()
            drawXAxis(canvas)
            drawChild(canvas)
            drawTools(canvas)
            drawLegend(canvas)
            drawYAxis(canvas)
            drawHisOrder(canvas)
            drawPriceLine(canvas)
            if (showSelected) {
                drawCross(canvas)
            }
        } else {
            drawEmpty(canvas)
        }
    }

    override fun onSelectedChange(event: MotionEvent) {
        if (mItemCount > 0) {
            val index = translateXToIndex(xToTranslateX(event.x))
            mSelectedIndex = if (index > mEndIndex) {
                mEndIndex
            } else if (index < mStartIndex) {
                mStartIndex
            } else {
                index
            }
            selectedY = event.y
            selectedX = event.x
            invalidate()
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        changeTranslate(mTranslateX + (l - oldl) * mScaleX)
        limitInvalidate()
    }

    override fun onScaleChanged(scale: Float, oldScale: Float) {
        if (scale == oldScale) {
            return
        }
        val tempWidth = mPointWidth * scale
        val newCount = viewWidth / tempWidth
        val oldCount = viewWidth / mPointWidth / oldScale
        val diffCount = (newCount - oldCount) / 2
        val dataWidth = getDataWidth()
        if (mStartIndex > 0) {
            changeTranslate(mTranslateX / oldScale * scale + diffCount * tempWidth)
        } else {
            if (dataWidth < viewWidth) {
                changeTranslate(dataWidth - viewWidth)
            } else {
                changeTranslate(getMaxTranslateX())
            }
        }

        limitInvalidate()
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        if (priceInLineRect.contains(e.x,e.y)){
            scrollToStart()
            return true
        }
        return super.onSingleTapUp(e)
    }

    private fun drawGrid(canvas: Canvas) {
        for ((i, e) in childRectMap.entries.withIndex()) {
            val childKey = e.key
            val childRect = e.value

            val bottomPosition = if (childKey == xBottomIndex) {
                childRect.bottom - mTextHeight
            } else {
                childRect.bottom.toFloat()
            }

            if (childKey == ChartType.MAIN) {
                val rowSpace = if (xBottomIndex == ChartType.MAIN) {
                    (childRect.height() - mTextHeight * 2) / gridRows
                } else {
                    (childRect.height() - mTextHeight) / gridRows
                }
                for (j in 0..gridRows) {
                    val stopY = rowSpace * j + childRect.top + mTextHeight
                    canvas.drawLine(0f, stopY, viewWidth, stopY, gridPaint)
                }
            }

            if (i == 0) {
                canvas.drawLine(
                    0f,
                    childRect.top.toFloat(),
                    viewWidth,
                    childRect.top.toFloat(),
                    gridPaint
                )
            }

            for (j in 1 until gridColumns) {
                val stopX = columSpace * j
                canvas.drawLine(
                    stopX,
                    childRect.top.toFloat(),
                    stopX,
                    bottomPosition,
                    gridPaint
                )
            }

            canvas.drawLine(
                0f,
                bottomPosition,
                viewWidth,
                bottomPosition,
                gridPaint
            )

            if (childKey == xBottomIndex) {
                canvas.drawLine(
                    0f,
                    childRect.bottom.toFloat(),
                    viewWidth,
                    childRect.bottom.toFloat(),
                    gridPaint
                )
            }
        }
    }

    private fun drawLogo(canvas: Canvas) {

    }

    private fun drawXAxis(canvas: Canvas) {
        val bottomPosition = when (xBottomIndex) {
            ChartType.BOTTOM_ALL -> (viewHeight - mTextHeight).toInt()
            else -> childRectMap[xBottomIndex]!!.bottom
        }
        val positionY = bottomPosition - (mTextHeight - mBaseLine)
        val halfWidth = mPointWidth / 2 * mScaleX
        val startX = indexToTranslateX(mStartIndex) - halfWidth
        val stopX = indexToTranslateX(mEndIndex) + halfWidth

        for (i in 0..gridColumns) {
            val tempX = columSpace * i
            val translateX = xToTranslateX(tempX)
            if (translateX in startX..stopX) {
                val index = translateXToIndex(translateX)
                val dateLong = getItem(index)?.getTime()
                if (dateLong != null) {
                    val text = mDateFormat.format(dateLong)
                    val mid = mTextPaint.measureText(text) / 2
                    canvas.drawText(text, tempX - mid, positionY, mTextPaint)
                }
            }
        }

    }

    private fun drawYAxis(canvas: Canvas) {
        for (entry in childRectMap) {
            val childName = entry.key
            val values = maxMinValueMap[childName]!!
            val maxValue = values[0]
            val minValue = values[1]
            val percent = values[2]
            when (childName) {
                ChartType.MAIN -> {
                    val rowValue = (maxValue - minValue) / gridRows
                    for (i in 1..gridRows) {
                        val positionY = rowSpace * i + mBaseLine
                        val text = mValueFormat.format((maxValue - i * rowValue).toString())
                        val textWidth = mTextPaint.measureText(text)
                        canvas.drawText(text, viewWidth - textWidth, positionY, mTextPaint)
                    }
                }

                ChartType.MACD -> {
                    val top = entry.value.top + mBaseLine + mTextHeight
                    val topText = mValueFormat.format(maxValue.toString())
                    val topTextWidth = mTextPaint.measureText(topText)
                    canvas.drawText(topText, viewWidth - topTextWidth, top, mTextPaint)
                }

                else -> {
                    val top = entry.value.top + mBaseLine + mTextHeight
                    val topText = mValueFormat.format(maxValue.toString())
                    val topTextWidth = mTextPaint.measureText(topText)
                    canvas.drawText(topText, viewWidth - topTextWidth, top, mTextPaint)

                    val bottom = entry.value.bottom - 2.dp
                    val bottomText = mValueFormat.format(minValue.toString())
                    val bottomTextWidth = mTextPaint.measureText(bottomText)
                    canvas.drawText(
                        bottomText,
                        viewWidth - bottomTextWidth,
                        bottom.toFloat(),
                        mTextPaint
                    )
                }
            }
        }
    }

    private fun drawChild(canvas: Canvas) {
        canvas.save()
        canvas.translate(mTranslateX, 0f)
        for (i in mStartIndex..mEndIndex) {
            val curPoint = getItem(i)
            val curX = indexToTranslateX(i)
            val prePoint = if (i == 0) curPoint else getItem(i - 1)
            val preX = if (i == 0) curX else indexToTranslateX(i - 1)
            for (child in chartMap) {
                val chartDraw = child.value
                chartDraw.draw(canvas, prePoint, curPoint, preX, curX, i, this)
            }
        }
        canvas.restore()
    }

    private fun drawLegend(canvas: Canvas) {
        val position = if (!showSelected) {
            if (mEndIndex > mItemCount - 1) mItemCount - 1 else mEndIndex
        } else mSelectedIndex

        for (child in chartMap) {
            val rect = childRectMap[child.key]
            val chartDraw = child.value
            chartDraw.drawText(
                canvas,
                this,
                mLegendMargin.toFloat(),
                rect!!.top + mBaseLine,
                getItem(position)
            )
        }
    }

    private fun drawEmpty(canvas: Canvas) {

    }

    /**
     * 十字星选择
     */
    private fun drawCross(canvas: Canvas) {
        val selectedX = indexToViewX(mSelectedIndex)
        //绘制时间数值
        canvas.drawLine(selectedX, 0f, selectedX, viewHeight, selectCrossPaint)
        val timeRect = childRectMap[xBottomIndex]!!
        val date = mDateFormat.format(getItem(mSelectedIndex)?.getTime())
        val textWidth = mAxisTextPaint.measureText(date)
        val dateLeft = selectedX - textWidth / 2
        val dateRight = selectedX + textWidth / 2
        mBorderBgPaint.color = crossBgColor
        canvas.drawRoundRect(
            dateLeft - mAxisPaddingH,
            timeRect.bottom - mTextHeight,
            dateRight + mAxisPaddingH,
            timeRect.bottom.toFloat(),
            mBorderRadius, mBorderRadius,
            mBorderBgPaint
        )
        canvas.drawText(date, dateLeft, timeRect.bottom - mTextHeight + mBaseLine, mAxisTextPaint)

        //绘制y轴数值
        for (entry in childRectMap) {
            val key = entry.key
            val rect = entry.value
            val bottomY = if (xBottomIndex == key) mTextHeight else 0f
            if (selectedY !in rect.top + mTextHeight..rect.bottom - bottomY) {
                continue
            }
            val iChartDraw = chartMap[key]!!
            canvas.drawLine(0f, selectedY, viewWidth, selectedY, selectCrossPaint)
            val value = yToValue(key, selectedY)
            val valueWidth =
                mAxisTextPaint.measureText(iChartDraw.getValueFormatter().format(value.toString()))
            val left = viewWidth - valueWidth
            canvas.drawRoundRect(
                left - mAxisPaddingH * 2,
                selectedY - mTextHeight / 2,
                viewWidth,
                selectedY + mTextHeight / 2,
                mBorderRadius, mBorderRadius,
                mBorderBgPaint
            )
            mMaxYLabelWidth = max(mMaxYLabelWidth, valueWidth + mAxisPaddingH * 2)
            canvas.drawText(
                iChartDraw.getValueFormatter().format(value.toString()),
                left - mAxisPaddingH,
                selectedY - mTextHeight / 2 + mBaseLine,
                mAxisTextPaint
            )

            canvas.drawCircle(selectedX, selectedY, 2f.dp, mBorderBgPaint)
        }

        drawSelector(canvas)
    }

    private fun drawSelector(canvas: Canvas) {
        if (mSelectedIndex == -1) {
            return
        }

        val item = getItem(mSelectedIndex) ?: return
        val diffValue = item.getClosePrice().toDouble() - item.getOpenPrice().toDouble()

        selectedPop[context.getString(R.string.kchart_selector_time)] =
            mDateFormat.format(item.getTime())
        selectedPop[context.getString(R.string.kchart_selector_open)] =
            mValueFormat.format(item.getOpenPrice())
        selectedPop[context.getString(R.string.kchart_selector_high)] =
            mValueFormat.format(item.getHighPrice())
        selectedPop[context.getString(R.string.kchart_selector_low)] =
            mValueFormat.format(item.getLowPrice())
        selectedPop[context.getString(R.string.kchart_selector_close)] =
            mValueFormat.format(item.getClosePrice())
        selectedPop[context.getString(R.string.kchart_selector_volume)] =
            mValueFormat.format(item.getVolume())
        selectedPop[context.getString(R.string.kchart_selector_amplitude)] =
            mValueFormat.format(diffValue.toString())
        selectedPop[context.getString(R.string.kchart_selector_amount)] =
            mValueFormat.format(item.getVolume())


        val paddingVertical = 4.dp
        val paddingHorizontal = 4.dp
        val popHeight = selectedPop.size * (mTextHeight + 2.dp) + paddingVertical * 2
        var maxWidth = 0f
        selectedPop.forEach { (key, value) ->
            maxWidth = max(maxWidth, mTextPaint.measureText("${key}${value}"))
        }
        val popWidth = maxWidth + 12.dp + paddingHorizontal * 2
        val bgRect = if (indexToViewX(mSelectedIndex) > viewWidth / 2) {
            RectF(5f.dp, mTextHeight + 2.dp, popWidth + 5f.dp, mTextHeight + 2f.dp + popHeight)
        } else {
            RectF(
                viewWidth - popWidth - mMaxYLabelWidth - 5f.dp,
                mTextHeight + 2.dp,
                viewWidth - mMaxYLabelWidth - 5f.dp,
                mTextHeight + 2f.dp + popHeight
            )
        }
        mBorderBgPaint.color = popBgColor
        canvas.drawRoundRect(bgRect, 20f, 20f, mBorderBgPaint)

        var tempTop = bgRect.top + paddingVertical + mTextHeight
        val startPos = bgRect.left + paddingHorizontal
        val endPos = bgRect.right - paddingHorizontal
        for ((index, key) in selectedPop.keys.withIndex()) {
            val value = selectedPop[key] ?: "--"
            canvas.drawText(key, startPos, tempTop, mTextPaint)
            val tempX = endPos - mTextPaint.measureText(value)
            canvas.drawText(value, tempX, tempTop, mTextPaint)
            tempTop += (mTextHeight + 2f.dp)
        }
    }

    private fun drawPriceLine(canvas: Canvas) {
        if (!mConfig.isOpenLastPrice && !mConfig.isOpenCountTimer) {
            return
        }
        val lastItem = getItem(mItemCount - 1)
        if (lastItem != null) {
            val closePrice = lastItem.getClosePrice()
            val lastY = valueToY(ChartType.MAIN, closePrice.toFloat())
            val lastPriceX = indexToViewX(mItemCount - 1)
            if (mEndIndex < mItemCount - 1) {
                canvas.drawLine(0f, lastY, viewWidth, lastY, selectCrossPaint)
                drawInLinePriceLabel(canvas,closePrice,lastY)
            } else {
                canvas.drawLine(lastPriceX, lastY, viewWidth, lastY, selectCrossPaint)
                drawEndPriceLabel(canvas, closePrice, lastY)
            }

//            if (mEndIndex < mItemCount - 1){
//                canvas.drawLine(0f,lastY,viewWidth,lastY,selectCrossPaint)
//            }else{
////                canvas.drawLine()
//            }
        }
    }

    private fun drawEndPriceLabel(canvas: Canvas, lastPrice: String, y: Float) {
        val formatPrice = mValueFormat.format(lastPrice)
        val lastPriceWidth = mTextPaint.measureText(formatPrice)
        val countDownWidth = mTextPaint.measureText(countDownStr)
        val startPos = viewWidth - lastPriceWidth - 4f.dp
        val endPos = viewWidth
        if (mConfig.isOpenLastPrice) {
            val rect = if (mConfig.isOpenCountTimer) {
                RectF(startPos, y - mTextHeight - 2f.dp, endPos, y + mTextHeight + 2f.dp)
            } else {
                RectF(startPos, y - mTextHeight / 2 - 2f.dp, endPos, y + mTextHeight / 2 + 2f.dp)
            }
            mBorderBgPaint.color = Color.WHITE
            canvas.drawRoundRect(rect, mBorderRadius, mBorderRadius, mBorderBgPaint)
            canvas.drawRoundRect(rect, mBorderRadius, mBorderRadius, mBorderPaint)
            canvas.drawText(
                formatPrice,
                rect.left + 2f.dp,
                y - mTextHeight / 2 + mBaseLine,
                mTextPaint
            )
        } else {
            if (mConfig.isOpenCountTimer) {

            } else {

            }
        }
    }

    private fun drawInLinePriceLabel(canvas: Canvas, lastPrice: String, y: Float) {
        priceInLineRect.setEmpty()
        val formatPrice = mValueFormat.format(lastPrice)
        val lastPriceWidth = mTextPaint.measureText(formatPrice)
        val countDownWidth = mTextPaint.measureText(countDownStr)
        val startPos = viewWidth - viewWidth / 3
        if (mConfig.isOpenLastPrice) {
            if (mConfig.isOpenCountTimer) {
                priceInLineRect.set(
                    startPos,
                    y - mTextHeight - 2f.dp,
                    startPos + max(lastPriceWidth,countDownWidth)+4f.dp,
                    y + mTextHeight + 2f.dp
                )
            } else {
                priceInLineRect.set(
                    startPos,
                    y - mTextHeight / 2 - 2f.dp,
                    startPos + lastPriceWidth + 4f.dp,
                    y + mTextHeight / 2 + 2f.dp
                )
            }
            mBorderBgPaint.color = Color.WHITE
            canvas.drawRoundRect(priceInLineRect, mBorderRadius, mBorderRadius, mBorderBgPaint)
            canvas.drawRoundRect(priceInLineRect, mBorderRadius, mBorderRadius, mBorderPaint)
            canvas.drawText(
                formatPrice,
                priceInLineRect.left + 2f.dp,
                y - mTextHeight / 2 + mBaseLine,
                mTextPaint
            )
        } else {
            if (mConfig.isOpenCountTimer) {

            } else {

            }
        }
    }

    private fun drawTools(canvas: Canvas) {
        val drawTools = SavedUtils.getDrawTools()
        for (tool in drawTools) {
            val startIndex = dateToIndex(tool.startTime) ?: return
            val endIndex = dateToIndex(tool.endTime) ?: return
            canvas.drawLine(
                indexToViewX(startIndex),
                valueToY(ChartType.MAIN, tool.startValue),
                indexToViewX(endIndex),
                valueToY(ChartType.MAIN, tool.endValue),
                mTextPaint
            )
        }
    }

    private fun drawHisOrder(canvas: Canvas) {

    }

    private fun drawNowEntrust(canvas: Canvas) {

    }

    private fun calculateValues() {
        maxMinValueMap.clear()
        val scaleWidth = mPointWidth * scaleX
        if (mTranslateX <= scaleWidth / 2) {
            mStartIndex = ((-mTranslateX) / scaleWidth).toInt()
        } else {
            mStartIndex = 0
        }
        mEndIndex = (mStartIndex + viewWidth / scaleWidth + 0.5).toInt() + 1
        if (mEndIndex > mItemCount - 1) {
            mEndIndex = mItemCount - 1
        }
//        mStartIndex = translateXToIndex(xToTranslateX(0f))
//        mEndIndex = translateXToIndex(xToTranslateX(viewWidth))
        "start = $mStartIndex, end = $mEndIndex".logd()

        for (child in chartMap.keys) {
            maxMinValueMap[child] = doubleArrayOf(Double.MIN_VALUE, Double.MAX_VALUE, 1.0)
        }

        for (i in mStartIndex..mEndIndex) {
            val item = getItem(i)
            if (item != null) {
                for (child in chartMap) {
                    val iChartDraw = child.value
                    val values = maxMinValueMap[child.key]
                    values!![0] = max(values[0], iChartDraw.getMaxValue(item))
                    values[1] = min(values[1], iChartDraw.getMinValue(item))
                    if (child.key == ChartType.MAIN) {
//                        if (mainHighMaxValue < item.getHighPrice().toFloat()) {
//                            mainHighMaxValue = item.getHighPrice().toFloat()
//                            mainMaxIndex = i
//                        }
//                        if (mainLowMinValue > item.getLowPrice().toFloat()) {
//                            mainLowMinValue = item.getLowPrice().toFloat()
//                            mainMinIndex = i
//                        }
                    }

                    val rect = childRectMap[child.key]!!
                    val diff = values[0] - values[1]
                    val timeHeight = if (xBottomIndex == child.key) mTextHeight else 0f
                    values[2] = if (child.key == ChartType.MAIN) {
                        (rect.height() - mTextHeight - timeHeight - mMainVPadding * 2) / diff
                    } else {
                        (rect.height() - mTextHeight - timeHeight) / diff
                    }

                    if (child.key == ChartType.MAIN) {
                        if (diff < 1e-15) {
                            values[0] += diff * 0.05f
                            values[1] -= diff * 0.05f
                        }
                    }
                }

            }

        }
        maxMinValueMap[ChartType.MAIN]?.forEach {
            it.logd()
        }
    }

    private fun getMaxTranslateX() = if (getDataWidth() > viewWidth) {
        if (selectedTime == TIME_LINE) 0f else mPointWidth * mScaleX / 2 + viewWidth / 2
    } else {
        viewHeight - getDataWidth() + overScrollRange - if (selectedTime == TIME_LINE) 0f else mPointWidth * mScaleX / 2
    }

    private fun getMinTranslateX() = if (getDataWidth() > viewWidth) {
        val minValue = viewWidth - getDataWidth() - viewWidth / 2 + overScrollRange
        if (overScrollRange == 0) minValue - mPointWidth * mScaleX / 2 else minValue
    } else {
        mPointWidth * mScaleX / 2
    }

    internal fun getRect(childType: String) = childRectMap[childType]

    private fun getItem(position: Int) =
        if (mAdapter == null || mAdapter!!.getCount() <= position || position < 0) {
            null
        } else {
            mAdapter?.getItem(position)
        }

    private fun dateToIndex(date: Long): Int? {
        val list = mAdapter?.getData() ?: return null
        return list.binarySearch {
            when {
                it.getTime() - date < 0L -> -1
                it.getTime() - date > 0L -> 1
                else -> 0
            }
        }
    }

    private fun changeTranslate(translateX: Float) {
        //TODO side listener
        mTranslateX = when {
            translateX < getMinTranslateX() -> {
                getMinTranslateX()
            }

            translateX > getMaxTranslateX() -> {
                getMaxTranslateX()
            }

            else -> {
                translateX
            }
        }

    }

    internal fun legendTextPaint() = mLegendTextPaint

    private fun limitInvalidate() {
        if (System.currentTimeMillis() - invalidateTime > 15) {
            invalidateTime = System.currentTimeMillis()
            invalidate()
        }
    }

    private fun formatCountdown(tick: Long) {
        val day = tick / 86400000
        val surplusMils = tick - day * 86400000
        val hour = surplusMils / 3600000
        val minute = (surplusMils - (hour * 3600000)) / 60000
        val second = (surplusMils - (hour * 3600000) - 60000 * minute) / 1000
        if (day >= 0 && hour >= 0 && minute >= 0 && second >= 0) {
            val strBuilder = StringBuilder()
            val minStr = if (minute < 10) "0$minute" else minute.toString()
            val secondStr = if (second < 10) "0$second" else second.toString()
            if (tick <= 3600000) {
                strBuilder.append(minStr).append(":").append(secondStr)
            } else {
                val hourStr = if (hour < 10) "0${hour}" else hour.toString()
                if (tick <= 86400000) {
                    strBuilder.append(hourStr).append(":").append(minStr).append(":")
                        .append(secondStr)
                } else {
                    strBuilder.append(day).append("D:").append(hourStr).append("H")
                }
            }
            val tempStr = strBuilder.toString()
            if (countDownStr != tempStr) {
                countDownStr = tempStr
                invalidate()
            }
        }

    }

    private fun scrollToStart(){
        showSelected = false
        val animator = ValueAnimator.ofFloat(mTranslateX, getMinTranslateX()).setDuration(400)
        animator.repeatCount = 0
        animator.addUpdateListener {
                changeTranslate(it.animatedValue as Float)
                limitInvalidate()
            }
        animator.start()
        mSelectedIndex = -1
    }

    fun changeTimeUnit(time: Long) {
        timeUnit = time
        countDownStr = "--"
        val lastPoint = getItem(mItemCount - 1)
        if (lastPoint != null) {
            val futureTime = lastPoint.getTime() + timeUnit - System.currentTimeMillis()
            mCountDown.reset(futureTime)
        }
    }

    fun setAdapter(adapter: IAdapter) = apply {
        mAdapter?.unregisterDataSetObserver(dataSetObserver)
        mAdapter = adapter
        mAdapter?.registerDataSetObserver(dataSetObserver)
        mItemCount = mAdapter?.getCount() ?: 0
    }

    fun setValueFormatter(valueFormat: IValueFormat) = apply {
        mValueFormat = valueFormat
        for (entry in chartMap) {
            entry.value.setValueFormatter(valueFormat)
        }
    }

    fun updateConfig(config: ChartConfig) {
        mConfig = config
        ConfigManger.saveConfig(config)
        invalidate()
    }

}
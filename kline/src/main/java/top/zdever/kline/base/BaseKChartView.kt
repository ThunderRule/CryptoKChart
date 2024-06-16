package top.zdever.kline.base

import android.content.Context
import android.database.DataSetObserver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import top.zdever.kline.ScrollAndScaleView
import top.zdever.kline.constants.BOTTOM_ALL
import top.zdever.kline.constants.CHILD_MACD
import top.zdever.kline.constants.CHILD_MAIN
import top.zdever.kline.constants.CHILD_VOLUME
import top.zdever.kline.constants.IconPosition
import top.zdever.kline.constants.ChildType
import top.zdever.kline.constants.MAIN_CANDLE
import top.zdever.kline.constants.TIME_15M
import top.zdever.kline.constants.TIME_LINE
import top.zdever.kline.constants.TimeType
import top.zdever.kline.draw.LineChart
import top.zdever.kline.draw.MACDChart
import top.zdever.kline.draw.MainChart
import top.zdever.kline.draw.VolumeChart
import top.zdever.kline.format.DefaultDateFormat
import top.zdever.kline.format.DefaultValueFormat
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
    private val DEFAULT_MAIN_VPADING = 10.dp
    private val DEFAULT_LEGEND_MARGIN = 10.dp

    protected var addedChildMap = hashMapOf<Int, IChartDraw>()
    protected var childRectMap = hashMapOf<Int, Rect>()
    protected var addedChilds = arrayListOf<Int>()
    protected var gridColumns = 4
    protected var gridRows = 4
    protected var logoIcon: Bitmap? = null
    protected var fullScreenIcon: Bitmap? = null
    protected var mTextHeight = 0f
    protected var mBaseLine = 0f
    protected val painXAxis = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val painYAxis = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val mTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    protected val gridPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    protected val selectCrossPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 0：max value， 1：min value， 2：scale
     */
    private val maxMinValueMap = hashMapOf<Int, FloatArray>()
    private var mMainHeight = DEFAULT_MAIN_HEIGHT
    private var mChildHeight = DEFAULT_SUB_HEIGHT
    private var mMainVPadding = DEFAULT_MAIN_VPADING
    private var mLegendMargin = DEFAULT_LEGEND_MARGIN
    private var viewWidth = 0f
    private var viewHeight = 0f
    private var columSpace = 0f
    private var rowSpace = 0f
    private var logoTop = 0f
    private var fullScreenTop = 0f
    private var logoPosition = IconPosition.BOTTOM_LEFT
    private var mAdapter: IAdapter? = null
    private var mStartIndex = 0
    private var mEndIndex = 0
    private var mTime = TIME_15M
    private var mainLineType = MAIN_CANDLE
    private var mainYScale = 1f
    private var xBottomIndex = CHILD_MAIN
    private var mDateFormat = DefaultDateFormat()
    private var mValueFormat = DefaultValueFormat()

    private var invalidateTime = 0L

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
        val dashPathEffect = DashPathEffect(floatArrayOf(3f.dp, 3f.dp), 0f)
        selectCrossPaint.setPathEffect(dashPathEffect)
        selectCrossPaint.strokeWidth = 1f.dp

        for (index in SavedUtils.getIndexes()) {
            addedChilds.add(index)
            when (index) {
                CHILD_MAIN -> addedChildMap[index] = MainChart(context)
                CHILD_MACD -> addedChildMap[index] = MACDChart(context)
                CHILD_VOLUME -> addedChildMap[index] = VolumeChart(context, index)
                else -> addedChildMap[index] = LineChart(context, index)
            }
            maxMinValueMap[index] = floatArrayOf(Float.MIN_VALUE, Float.MAX_VALUE, 0f)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val totalHeight = if (addedChilds.contains(CHILD_MAIN)) {
            mMainHeight + (addedChildMap.size - 1) * mChildHeight + mTextHeight
        } else {
            addedChildMap.size * mChildHeight + mTextHeight
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
        for (childName in addedChilds) {
            val chartHeight = if (childName == CHILD_MAIN) {
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
            invalidate()
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        changeTranslate(mTranslateX + (l - oldl) * mScaleX)
        limitInvalidate()
    }

    override fun onScaleChanged(scale: Float, oldScale: Float) {
        if (scale == oldScale){
            return
        }
        val tempWidth = mPointWidth * scale
        val newCount = viewWidth / tempWidth
        val oldCount = viewWidth/mPointWidth / oldScale
        val diffCount = (newCount - oldCount)/2
        val dataWidth = getDataWidth()
        if (mStartIndex > 0){
            changeTranslate(mTranslateX / oldScale * scale + diffCount * tempWidth)
        }else{
            if(dataWidth < viewWidth){
                changeTranslate(dataWidth - viewWidth)
            }else{
                changeTranslate(getMaxTranslateX())
            }
        }

        limitInvalidate()
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

            if (childKey == CHILD_MAIN) {
                val rowSpace = if (xBottomIndex == CHILD_MAIN) {
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
        }
    }

    private fun drawLogo(canvas: Canvas) {

    }

    private fun drawXAxis(canvas: Canvas) {
        val bottomPosition = when (xBottomIndex) {
            BOTTOM_ALL -> (viewHeight - mTextHeight).toInt()
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
                CHILD_MAIN -> {
                    val rowValue = (maxValue - minValue) / gridRows
                    for (i in 1..gridRows) {
                        val positionY = rowSpace * i + mBaseLine
                        val text = mValueFormat.format((maxValue - i * rowValue).toString())
                        val textWidth = mTextPaint.measureText(text)
                        canvas.drawText(text, viewWidth - textWidth, positionY, mTextPaint)
                    }
                }

                else -> {

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
            val position = if (mSelectedIndex == -1) mEndIndex else mSelectedIndex
            for (child in addedChildMap) {
                val rect = childRectMap[child.key]
                val chartDraw = child.value
                chartDraw.draw(canvas, prePoint, curPoint, preX, curX, i, this)
                chartDraw.drawText(
                    canvas,
                    this,
                    position,
                    mLegendMargin.toFloat(),
                    rect!!.top + mBaseLine
                )
            }
        }
        canvas.restore()
    }

    private fun drawEmpty(canvas: Canvas) {

    }

    private fun drawCross(canvas: Canvas) {
        val selectedX = indexToViewX(mSelectedIndex)
        canvas.drawLine(selectedX, 0f, selectedX, viewHeight, selectCrossPaint)

    }

    private fun drawPriceLine(canvas: Canvas) {
        val lastItem = getItem(mItemCount - 1)
        if (lastItem != null) {
            val lastY = valueToY(CHILD_MAIN, lastItem.getClosePrice().toFloat())
//            if (mEndIndex < mItemCount - 1){
//                canvas.drawLine(0f,lastY,viewWidth,lastY,selectCrossPaint)
//            }else{
////                canvas.drawLine()
//            }
        }
    }

    private fun drawHisOrder(canvas: Canvas) {

    }

    private fun drawNowEntrust(canvas: Canvas) {

    }

    private fun calculateValues() {
        maxMinValueMap.clear()
        mStartIndex = translateXToIndex(xToTranslateX(0f))
        mEndIndex = translateXToIndex(xToTranslateX(viewWidth))

        for (child in addedChilds) {
            maxMinValueMap[child] = floatArrayOf(Float.MIN_VALUE, Float.MAX_VALUE, 1f)
        }

        "startIndex = $mStartIndex, endIndex = $mEndIndex".logd()
        for (i in mStartIndex..mEndIndex) {
            val item = getItem(i)
            if (item != null) {
                for (child in addedChildMap) {
                    val iChartDraw = child.value
                    val values = maxMinValueMap[child.key]
                    values!![0] = max(values[0], iChartDraw.getMaxValue(item))
                    values[1] = min(values[1], iChartDraw.getMinValue(item))
                    if (child.key == CHILD_MAIN) {
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
                    values[2] = if (child.key == CHILD_MAIN) {
                        (rect.height() - mTextHeight * 2 - mMainVPadding) / diff
                    } else {
                        (rect.height() - mTextHeight) / diff
                    }

                    if (child.key == CHILD_MAIN) {
                        if (diff < 1e-15) {
                            values[0] += diff * 0.05f
                            values[0] -= diff * 0.05f
                        }
                    }
                }

            }

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

    private fun isFillScreen() = getDataWidth() >= viewWidth / mScaleX

    internal fun valueToY(@ChildType childType: Int, value: Float): Float {
        val rect = childRectMap[childType]!!
        val maxMinValues = maxMinValueMap[childType]!!
        return if (childType == CHILD_MAIN){
            val topLimit = rect.top + mTextHeight + mMainVPadding
            val tempValue = topLimit + (maxMinValues[0] - value) * maxMinValues[2]
            val bottomLimit = rect.bottom - mTextHeight - mMainVPadding
            when{
                tempValue > bottomLimit -> bottomLimit
                tempValue < topLimit -> topLimit
                else -> tempValue
            }
        }else{
            val topLimit = rect.top + mTextHeight
            val tempValue = topLimit + (maxMinValues[0] - value) * maxMinValues[2]
            when{
                tempValue < topLimit -> topLimit
                tempValue > rect.bottom -> rect.bottom.toFloat()
                else -> tempValue
            }
        }
    }

    internal fun getMaxMinValues(@ChildType childType: Int) = maxMinValueMap[childType]

    internal fun getRect(@ChildType childType: Int) = childRectMap[childType]

    private fun getItem(position: Int) =
        if (mAdapter == null || mAdapter!!.getCount() <= position || position < 0) {
            null
        } else {
            mAdapter?.getItem(position)
        }

    private fun changeTranslate(translateX: Float) {
        //TODO side listener
        when {
            translateX < getMinTranslateX() -> {
                mTranslateX = getMinTranslateX()
            }

            translateX > getMaxTranslateX() -> {
                mTranslateX = getMaxTranslateX()
            }

            else -> {
                mTranslateX = translateX
            }
        }


    }

    private fun limitInvalidate() {
        if (System.currentTimeMillis() - invalidateTime > 15) {
            invalidateTime = System.currentTimeMillis()
            invalidate()
        }
    }

    fun setAdapter(adapter: IAdapter) {
        mAdapter?.unregisterDataSetObserver(dataSetObserver)
        mAdapter = adapter
        mAdapter?.registerDataSetObserver(dataSetObserver)
        mItemCount = mAdapter?.getCount() ?: 0
    }

}
package top.zdever.kline.base

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.FrameLayout
import android.widget.OverScroller
import top.zdever.kline.constants.ChartType
import top.zdever.kline.constants.SelectType
import top.zdever.kline.listener.OnScrollListener
import top.zdever.kline.model.DrawLineEntity
import top.zdever.kline.utils.SavedUtils
import top.zdever.kline.utils.dp
import top.zdever.kline.utils.logd
import kotlin.math.round

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/10
 */
abstract class ScrollAndScaleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), GestureDetector.OnGestureListener,
    ScaleGestureDetector.OnScaleGestureListener {

    private val DEFAULT_MAIN_VPADING = 10.dp

    protected var mScrollX = 0f
    protected var showSelected = false
    protected var forceStopSlid = false
    protected var touch = false
    protected var mScaleX = 1f
    protected var scaleXMax = 6f
    protected var scaleXMin = 0.2f
    private var isMultipleTouch = false
    private var isScrollEnable = true
    private var isScaleEnable = true
    protected var selectModel = SelectType.SELECT_BOTH
    protected var mTranslateX = Float.MIN_VALUE
    internal var mPointWidth = 6f.dp
    protected var mItemCount = 0
    protected var overScrollRange = 100.dp
    protected var mSelectedIndex = -1
    private var isEditMode = false
    protected var mAdapter: IAdapter? = null
    private var tempLine:DrawLineEntity? = null

    /**
     * 0：max value， 1：min value， 2：scale
     */
    protected val maxMinValueMap = hashMapOf<String, DoubleArray>()
    protected var childRectMap = hashMapOf<String, Rect>()
    protected var mTextHeight = 0f
    protected var mMainVPadding = DEFAULT_MAIN_VPADING
    protected var xBottomIndex = ChartType.MAIN

    private var mScrollListener: OnScrollListener? = null

    protected val overScroller: OverScroller
    protected val gestureDetector: GestureDetector
    protected val scaleDetector: ScaleGestureDetector

    init {
        setWillNotDraw(false)
        gestureDetector = GestureDetector(context, this)
        scaleDetector = ScaleGestureDetector(context, this)
        overScroller = OverScroller(context)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)

        if (event.pointerCount > 1 || isEditMode) {
            showSelected = false
            mSelectedIndex = -1
        }
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                forceStopSlid = false
                touch = true
            }

            MotionEvent.ACTION_MOVE -> {
                if (showSelected) {
                    onSelectedChange(event)
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                touch = false
                if (isEditMode){
                    if (tempLine == null){
                        tempLine = createLine(event)
                    }else{
                        closeLine(event,tempLine)
                    }
                }
                invalidate()
            }

            MotionEvent.ACTION_CANCEL -> {
                showSelected = false
                mSelectedIndex = -1
                touch = false
                invalidate()
            }

            else -> {}
        }
        isMultipleTouch = event.pointerCount > 1
        gestureDetector.onTouchEvent(event)
        scaleDetector.onTouchEvent(event)
        return true
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {

    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        when (selectModel) {
            SelectType.SELECT_TOUCH -> {
                showSelected = true
                onSelectedChange(e)
            }

            SelectType.SELECT_PRESS -> {
                showSelected = true
            }

            SelectType.SELECT_BOTH -> {
                if (showSelected) {
                    showSelected = false
                } else {
                    showSelected = true
                    onSelectedChange(e)
                }
            }

            SelectType.SELECT_NONE -> {

            }
        }
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        showSelected = false
        if (!isMultipleTouch && isScrollEnable && !isEditMode) {
            scrollBy(round(distanceX).toInt(), 0)
            return true
        }
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        if (selectModel == SelectType.SELECT_PRESS || selectModel == SelectType.SELECT_BOTH) {
            showSelected = true
            onSelectedChange(e)
        }
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (!showSelected && !touch && isScrollEnable) {
            overScroller.fling(
                mScrollX.toInt(),
                0,
                round(velocityX / mScaleX).toInt(),
                0,
                Int.MIN_VALUE,
                Int.MAX_VALUE,
                0,
                0
            )
        }
        return true
    }

    override fun computeScroll() {
        if (overScroller.computeScrollOffset()) {
            if (!touch && isScrollEnable && !isEditMode) {
                scrollTo(overScroller.currX, overScroller.currY)
            } else {
                overScroller.forceFinished(true)
            }
            invalidate()
        }
    }

    override fun scrollBy(x: Int, y: Int) {
        if (isScaleEnable) {
            scrollTo((mScrollX - round(x / mScaleX)).toInt(), 0)
        } else {
            overScroller.forceFinished(true)
        }
    }

    override fun scrollTo(x: Int, y: Int) {
        if (isScrollEnable) {
            val oldX = mScrollX
            mScrollX = x.toFloat()
            if (mScrollX != oldX) {
                onScrollChanged(mScrollX.toInt(), 0, oldX.toInt(), 0)
            }
        } else {
            overScroller.forceFinished(true)
        }
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (!isScaleEnable) {
            return false
        }
        val oldScale = mScaleX
        mScaleX *= detector.scaleFactor
        if (mScaleX < scaleXMin) {
            mScaleX = scaleXMin
        } else if (mScaleX > scaleXMax) {
            mScaleX = scaleXMax
        }
        onScaleChanged(mScaleX, oldScale)

        return true
    }

    override fun getScaleX(): Float {
        return mScaleX
    }

    override fun setScrollX(value: Int) {
        mScrollX = value.toFloat()
        scrollTo(value, 0)
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {

    }

    protected open fun onScaleChanged(scale: Float, oldScale: Float) {

    }

    /**
     * view x to translateX
     */
    protected fun xToTranslateX(x: Float) = -mTranslateX + x

    protected fun translateXToX(translateX: Float) = translateX + mTranslateX

    protected fun translateXToIndex(translateX: Float): Int {
        return if (getDataWidth() < width) {
            (translateX + mTranslateX) / mPointWidth / mScaleX + 0.5f
        } else {
            translateX / mPointWidth / mScaleX
        }.toInt()
    }

    protected fun indexToTranslateX(index: Int): Float = index * mPointWidth * mScaleX

    protected fun indexToViewX(index: Int): Float {
        val diffTranslate = indexToTranslateX(index)+mTranslateX
        """
            t = $mTranslateX,
            it = ${indexToTranslateX(index)},
            diff = $diffTranslate
        """.trimIndent().logd()
        return diffTranslate
    }

    protected fun getDataWidth() = (mItemCount - 1) * mPointWidth * scaleX + overScrollRange


    private fun createLine(event: MotionEvent): DrawLineEntity? {
        mAdapter ?: return null
        val index = translateXToIndex(xToTranslateX(event.x))
        val time = mAdapter!!.getItem(index).getTime()
        val value = yToValue(ChartType.MAIN, event.y)
        return DrawLineEntity(System.currentTimeMillis(),time,value)
    }

    private fun closeLine(event: MotionEvent, drawLineEntity: DrawLineEntity?){
        mAdapter ?: return
        val index = translateXToIndex(xToTranslateX(event.x))
        drawLineEntity?.endTime = mAdapter!!.getItem(index).getTime()
        drawLineEntity?.endValue = yToValue(ChartType.MAIN, event.y)
        SavedUtils.saveDrawTool(drawLineEntity?.copy())
        tempLine = null
    }

    internal fun valueToY(childType: String, value: Float): Float {
        val rect = childRectMap[childType]!!
        val maxMinValues = maxMinValueMap[childType]!!
        return if (childType == ChartType.MAIN) {
            val topLimit = rect.top + mTextHeight + mMainVPadding
            val tempY = topLimit + (maxMinValues[0] - value) * maxMinValues[2]
            if (tempY<topLimit){
                topLimit
            }else if (tempY > rect.bottom - mTextHeight - mMainVPadding){
                rect.bottom - mTextHeight - mMainVPadding
            }else{
                tempY
            }
        } else {
            val topLimit = rect.top + mTextHeight
            topLimit + (maxMinValues[0] - value) * maxMinValues[2]
        }.toFloat()
    }

    internal fun yToValue(childType: String, y: Float): Float {
        val rect = childRectMap[childType]!!
        val maxMinValues = maxMinValueMap[childType]!!
        val timeHeight = if (childType == xBottomIndex) mTextHeight else 0f
        val value = when (childType) {
            ChartType.MAIN -> {
                maxMinValues[1] + (maxMinValues[0] - maxMinValues[1]) / (rect.height() - timeHeight - mTextHeight - mMainVPadding * 2) * (rect.bottom - timeHeight - mMainVPadding - y)
            }

            ChartType.MACD -> {
                val diff = maxMinValues[0] - maxMinValues[1]
                val zeroLine = valueToY(ChartType.MACD, 0f)
                val everyHeight = diff / (rect.height() - timeHeight - mTextHeight)
                everyHeight * (zeroLine - y)
            }

            else -> {
                maxMinValues[0] / (rect.height() - timeHeight - mTextHeight) * (rect.bottom - timeHeight - y)
            }
        }
        return value.toFloat()
    }

    fun openEdit() {
        isEditMode = true
    }

    fun closeEdit() {
        isEditMode = false
    }

    abstract fun onSelectedChange(event: MotionEvent)

    internal fun getSelectedIndex() = mSelectedIndex


}
package top.zdever.kline

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.FrameLayout
import android.widget.OverScroller
import top.zdever.kline.constants.SelectType
import top.zdever.kline.listener.OnScrollListener
import top.zdever.kline.utils.dp
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

    private var mScrollListener:OnScrollListener? = null

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

        if (event.pointerCount > 1) {
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
                if (showSelected){
                    showSelected = false
                }else{
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
        if (!isMultipleTouch || isScrollEnable){
            scrollBy(round(distanceX).toInt(),0)
            return true
        }
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        if (selectModel == SelectType.SELECT_PRESS || selectModel == SelectType.SELECT_BOTH){
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
            if (!touch && isScrollEnable) {
                scrollTo(overScroller.currX, overScroller.currY)
            } else {
                overScroller.forceFinished(true)
            }
            invalidate()
        }
    }

    override fun scrollBy(x: Int, y: Int) {
        if (isScaleEnable){
            scrollTo((mScrollX - round(x / mScaleX)).toInt(), 0)
        }else{
            overScroller.forceFinished(true)
        }
    }

    override fun scrollTo(x: Int, y: Int) {
        if (isScrollEnable){
            val oldX = mScrollX
            mScrollX = x.toFloat()
            if (mScrollX != oldX){
                onScrollChanged(mScrollX.toInt(),0, oldX.toInt(),0)
            }
        }else{
            overScroller.forceFinished(true)
        }
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (!isScaleEnable){
            return false
        }
        val oldScale = mScaleX
        mScaleX *= detector.scaleFactor
        if (mScaleX < scaleXMin){
            mScaleX = scaleXMin
        }else if (mScaleX > scaleXMax){
            mScaleX = scaleXMax
        }
        onScaleChanged(mScaleX,oldScale)

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

    protected fun translateXToIndex(translateX: Float):Int{
        return if (getDataWidth() < width){
            (translateX + mTranslateX) / mPointWidth / mScaleX + 0.5f
        }else{
            translateX / mPointWidth / mScaleX
        }.toInt()
    }

    protected fun indexToTranslateX(index: Int): Float = index * mPointWidth * mScaleX

    protected fun indexToViewX(index: Int):Float{
        val leftIndex = translateXToIndex(xToTranslateX(0f))
        val diff = index - leftIndex
        return indexToTranslateX(diff)
    }

    protected fun getDataWidth() = (mItemCount - 1) * mPointWidth * scaleX + overScrollRange

    abstract fun onSelectedChange(event: MotionEvent)

}
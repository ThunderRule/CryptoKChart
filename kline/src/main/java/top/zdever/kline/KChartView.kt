package top.zdever.kline

import android.content.Context
import android.util.AttributeSet
import top.zdever.kline.base.BaseKChartView
import top.zdever.kline.utils.dp
import top.zdever.kline.utils.sp

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/11
 */
class KChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseKChartView(context, attrs, defStyle) {

    init {
        val osa = context.obtainStyledAttributes(attrs, R.styleable.KChartView, defStyle, 0)

        setTextSize(osa.getDimension(R.styleable.KChartView_chart_textSize,12f.sp))

        osa.recycle()
    }

    fun setTextSize(size: Float) {
        mTextPaint.textSize = size
        val fm = mTextPaint.fontMetrics
        mTextHeight = fm.descent - fm.ascent
        mBaseLine = (mTextHeight - fm.bottom - fm.top)/2
    }


}
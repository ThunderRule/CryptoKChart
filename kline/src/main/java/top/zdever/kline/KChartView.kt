package top.zdever.kline

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import top.zdever.kline.base.BaseKChartView
import top.zdever.kline.config.ChartConfig
import top.zdever.kline.utils.color
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
        crossBgColor = osa.getColor(R.styleable.KChartView_chart_crossBgColor,Color.BLACK)
        popBgColor = osa.getColor(R.styleable.KChartView_chart_popBgColor,Color.WHITE)
        setAxisTextColor(Color.WHITE)

        osa.recycle()
    }

    fun config(config: ChartConfig){
        setTextSize(10f)
    }

    private fun setTextSize(size: Float) {
        mTextPaint.textSize = size
        mAxisTextPaint.textSize = size
        mLegendTextPaint.textSize = size
        val fm = mTextPaint.fontMetrics
        mTextHeight = fm.descent - fm.ascent
        mBaseLine = (mTextHeight - fm.bottom - fm.top)/2
    }

    fun setAxisTextColorRes(@ColorRes color:Int){
        setAxisTextColor(context.color(color))
    }

    private fun setAxisTextColor(@ColorInt color: Int){
        mAxisTextPaint.color = color
    }


}
package top.zdever.kline.utils

import android.os.CountDownTimer

/**
 * @description
 *
 * @author Draksum
 * @createDate 2025/7/30
 */
class CountDownUtils {
    private var countDownTimer:CountDownTimer? = null
    private var remainingTime:Long = Long.MAX_VALUE
    private var isRunning = false
    private var mTickListener:((Long)->Unit)? = null

    fun start(){
        if (isRunning) return

        countDownTimer = object : CountDownTimer(remainingTime,1000){
            override fun onTick(millisUntilFinished: Long) {
                mTickListener?.invoke(millisUntilFinished)
            }

            override fun onFinish() {
                isRunning = false
            }

        }.start()

        isRunning = true
    }

    fun pause(){
        countDownTimer?.cancel()
        isRunning = false
    }

    fun reset(newTime:Long){
        remainingTime = newTime
        pause()
        start()
    }

    fun setOnTick(listener:(Long)->Unit){
        mTickListener = listener
    }
}
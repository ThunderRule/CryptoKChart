package top.zdever.klinedemo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.zdever.kline.KChartView
import top.zdever.kline.adapter.KLineEntityAdapter
import top.zdever.klinedemo.network.BinanceApis
import top.zdever.klinedemo.network.Http

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val adapter = KLineEntityAdapter()
        val kChartView = findViewById<KChartView>(R.id.kchart)
        kChartView.setAdapter(adapter)
        lifecycleScope.launch {
            val klines = Http.create(BinanceApis::class.java).klines("BTCUSDT", "15m")
            if (!klines.isNullOrEmpty()) {
                val klineList = arrayListOf<KLineBean>()
                for (kline in klines) {
                    klineList.add(
                        KLineBean(
                            o = kline[1] as String,
                            h = kline[2] as String,
                            l = kline[3] as String,
                            c = kline[4] as String,
                            vol = kline[5] as String,
                            date = (kline[0] as Double).toLong()
                        )
                    )
                }
                launch(Dispatchers.Main) {
                    adapter.addData(klineList)
                }
            }
        }
    }
}
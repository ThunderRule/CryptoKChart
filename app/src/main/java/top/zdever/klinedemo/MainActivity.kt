package top.zdever.klinedemo

import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.zdever.kline.KChartView
import top.zdever.kline.adapter.KLineEntityAdapter
import top.zdever.kline.config.ConfigManger
import top.zdever.kline.config.MainConfig
import top.zdever.kline.constants.ChartType
import top.zdever.kline.constants.IndexType
import top.zdever.kline.format.IValueFormat
import top.zdever.klinedemo.network.BinanceApis
import top.zdever.klinedemo.network.Http
import top.zdever.klinedemo.network.PublicWsManager
import java.util.Locale

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

//        PublicWsManager.connect("wss://stream.binance.com:443/")
//        PublicWsManager.subKline("15m","btcusdt")

        val chartConfig = ConfigManger.getConfig()

        val adapter = KLineEntityAdapter()
        val kChartView = findViewById<KChartView>(R.id.kchart)
        kChartView.setAdapter(adapter)
            .setValueFormatter(object :IValueFormat{
                override fun format(value: String?): String {
                    value?:return "--"
                    return String.format(Locale.US, "%.2f", value.toFloat())
                }

            })

        lifecycleScope.launch {
            val klines = Http.create(BinanceApis::class.java).klines("ETHUSDT", "15m")
            if (!klines.isNullOrEmpty()) {
                val klineList = arrayListOf<KLineBean>()
                for (kline in klines) {
                    klineList.add(
                        KLineBean(
                            o = kline[1] as String,
                            h = kline[2] as String,
                            l = kline[3] as String,
                            c = kline[4] as String,
                            a = kline[10] as String,
                            vol = kline[5] as String,
                            date = (kline[0] as Double).toLong()
                        )
                    )
                }
                launch(Dispatchers.Main) {
                    adapter.addData(klineList.takeLast(20))
                }
            }
        }

        findViewById<Switch>(R.id.s_edit).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                kChartView.openEdit()
            }else{
                kChartView.closeEdit()
            }
        }

        val rgGroup = findViewById<RadioGroup>(R.id.rgGroup)
        rgGroup.setOnCheckedChangeListener { group, checkedId ->
            val mainConfig = chartConfig.childConfig[ChartType.MAIN] as MainConfig
            when (checkedId) {
                R.id.rbMA -> {
                    mainConfig.indicator = IndexType.MA
                }

                R.id.rbEMA -> {
                    mainConfig.indicator = IndexType.EMA
                }

                R.id.rbBOLL -> {
                    mainConfig.indicator = IndexType.BOLL
                }

                R.id.rbSAR -> {
                    mainConfig.indicator = IndexType.SAR
                }

                else -> {}
            }
            kChartView.updateConfig(chartConfig)
        }


    }
}
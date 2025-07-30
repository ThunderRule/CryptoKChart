package top.zdever.kline.config

import android.content.Context
import com.google.gson.Gson
import top.zdever.kline.utils.AppProvider

/**
 * @description
 *
 * @author Draksum
 * @createDate 2025/7/28
 */
object ConfigManger {

    private const val CONFIG_FILE = "chart_configs.json"
    private val mGson = Gson()

    private var config:ChartConfig? = null
    private val defaultConfig = ChartConfig()

    fun setChildDefault(name:String,config:IConfig){
        defaultConfig.childConfig[name] = config
    }


    fun getConfig():ChartConfig{
        if (config == null){
            config = loadConfig()
        }
        return config!!
    }

    fun saveConfig(config:ChartConfig){
        val json = mGson.toJson(config)
        AppProvider.getAppContext().openFileOutput(CONFIG_FILE,Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
    }

    fun loadConfig():ChartConfig?{
        return try {
            AppProvider.getAppContext().openFileInput(CONFIG_FILE).bufferedReader().use { reader->
                val json = reader.readText()
                mGson.fromJson(json,ChartConfig::class.java)
            }
        }catch (e:Exception){
            defaultConfig
        }
    }

}
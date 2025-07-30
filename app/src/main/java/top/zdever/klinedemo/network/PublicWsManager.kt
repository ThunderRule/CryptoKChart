package top.zdever.klinedemo.network

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * @description
 *
 * @author Draksum
 * @createDate 2025/7/28
 */
object PublicWsManager {
    private const val TAG = "PublicWsManager"

    private val mWsClient by lazy {
        WsEngin("public")
    }

    private val mSubscribeCache = hashSetOf<String>()

    init {
        mWsClient.setOnOpenListener {
            for (sub in mSubscribeCache) {
                it.send(sub)
            }
        }

        mWsClient.setOnMsg {
            Log.d(TAG, "msg : $it")
        }
    }

    fun subKline(time:String,symbol:String){
        val jsonObject = JSONObject()
        jsonObject.put("method","SUBSCRIBE")
        jsonObject.put("id",1)
        val jsonArray = JSONArray()
        jsonArray.put("$symbol@kline_$time")
        jsonObject.put("params",jsonArray)
        val json = jsonObject.toString()
        mSubscribeCache.add(json)
        mWsClient.send(json)
    }

    fun connect(url:String){
        mWsClient.connect(url)
    }
}
package top.zdever.klinedemo.network

import android.util.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.Buffer
import okio.ByteString
import okio.GzipSource
import okio.Source
import okio.buffer
import java.util.concurrent.TimeUnit

/**
 * @description
 *
 * @author Draksum
 * @createDate 2023/10/16
 */
class WsEngin(private val channel:String) : WebSocketListener() {
    private val TAG = "WsEngin"
    private val mainScope = MainScope()
    private val mClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .pingInterval(5, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
    private var reconnectJob: Job? = null

    private var mUrl = ""
    private var mWebSocket: WebSocket? = null
    private var isConnected = false
    private var mOnOpen: ((WebSocket) -> Unit)? = null
    private var mOnClose: (() -> Unit)? = null
    private var mOnMsg: ((String) -> Unit)? = null
    private var startTimeMillis = 0L
    private var isFirstConnect = true

    fun connect(url: String) {
        reconnectJob?.cancel()
        mWebSocket?.cancel()
        mUrl = url
        val request = Request.Builder()
            .url(url)
            .build()
        mWebSocket = mClient.newWebSocket(request, this)
    }

    fun isConnect() = mWebSocket != null && isConnected

    private fun reconnect() {
        reconnectJob = mainScope.launch {
            if (!isConnected) {
                delay(5000)
                mWebSocket?.cancel()
                connect(mUrl)
            }
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        isConnected = response.code() == 101
        mOnOpen?.invoke(webSocket)
        isFirstConnect = true
        startTimeMillis = System.currentTimeMillis()
        Log.d(TAG, "onOpen: ")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        if (text.contains("ping",true)){
            send("PONG")
        }else{
            mOnMsg?.invoke(text)
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        isConnected = false
        mOnClose?.invoke()
        Log.d(TAG, "onClosed: ")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        isConnected = false
        mOnClose?.invoke()
        Log.d(TAG, "onClosing: ")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        isConnected = false
        if (t.message != "Socket closed") {
            reconnect()
        }
        mOnClose?.invoke()
        Log.d(TAG, "onFailure: ${t.message}")
    }

    fun setOnOpenListener(onOpen: (WebSocket) -> Unit) {
        mOnOpen = onOpen
    }

    fun setOnOnCloseListener(onClose: () -> Unit) {
        mOnClose = onClose
    }

    fun setOnMsg(onMsg: (String) -> Unit) {
        mOnMsg = onMsg
    }

    fun send(msg: String) {
        if (isConnect()) {
            mWebSocket?.send(msg)
        }
    }

    fun close() {
        if (isConnect()) {
            mWebSocket?.cancel()
            mWebSocket?.close(1000, "close")
        }
    }
}
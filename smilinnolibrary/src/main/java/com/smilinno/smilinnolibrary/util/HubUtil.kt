package com.smilinno.smilinnolibrary.util

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.TransportEnum
import com.smilinno.smilinnolibrary.apistate.ApiState
import com.smilinno.smilinnolibrary.callback.PlayerListener
import com.smilinno.smilinnolibrary.callback.SmilinnoListener
import com.smilinno.smilinnolibrary.model.MessageResponse
import com.smilinno.smilinnolibrary.model.MessageType
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking


internal object HubUtil {
    private lateinit var playTTS: (voiceUrl: String) -> Unit
    private val TAG: String = HubUtil::class.java.simpleName
    private lateinit var hubConnection: HubConnection
    private var retryConnectionTime = 1000L
    var smilinnoListener: SmilinnoListener? = null
        set(value) {
            field = value
            sendConnectionState()
        }
    private val ACCESS_TOKEN_KEY = "access_token"
    private val TEXTMESSAGE = "TextMessage"
    private val VOICEMESSAGE = "UserMessage"
    private val MESSAGE = "UserMessage"
    private val TOKENERROR = "TokenError"
    private val PAYINGTHEBILL = "PayingTheBill"
    private val ACCOUNTBILL = "AccountBill"
    private val ACCOUNTBALANCE = "AccountBalance"
    private val MONEYTRANSFER = "MoneyTransfer"
    private val UNRELATED = "UnRelated"
    private val ERROR = "Error"

    private val chatStateFlow: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    val _chatStateFlow: StateFlow<ApiState> = chatStateFlow

    private val connectionStateFlow: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    val _connectionStateFlow: StateFlow<ApiState> = connectionStateFlow

    private val chatHistoryStateFlow: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Empty)
    val _chatHistoryStateFlow: StateFlow<ApiState> = chatHistoryStateFlow

    private val deleteHistoryStateFlow: MutableStateFlow<ApiState> =
        MutableStateFlow(ApiState.Empty)
    val _deleteHistoryStateFlow: StateFlow<ApiState> = deleteHistoryStateFlow


    fun initSignalRHubConnection(token: String, retryConnectionTime: Long?, playTTS : (voiceUrl : String) -> Unit ) {
        try {
            retryConnectionTime?.let {
                this.retryConnectionTime = it
            }
            this.playTTS = playTTS
            hubConnection = HubConnectionBuilder
//                http://37.32.24.190:9104/chathub_v1_5
//                https://assistant.smilinno-dev.com/hub
                .create("https://assistant.smilinno-dev.com/hub")
                .withHeader(ACCESS_TOKEN_KEY, token)
                .shouldSkipNegotiate(true)
                .withHandshakeResponseTimeout(15 * 1000)
                .withTransport(TransportEnum.WEBSOCKETS)
                .build()
            hubConnection.serverTimeout = 30 * 1000
            hubConnection.keepAliveInterval = 10 * 1000
            hubConnection.onClosed {
                Log.e(TAG, "hubConnection is closed!", it)
                sendConnectionState()
                startHubConnection()
            }
            getTextFromServer()
            getErrorFromServer()
            getUnRelatedFromServer()
            getMoneyTransferFromServer()
            getAccountBalanceFromServer()
            getAccountBillFromServer()
            getPayingTheBillFromServer()
            getTokenErrorFromServer()
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "initSignalRHubConnection exception:", e)
        }

    }

    fun startHubConnection() {

        try {
            hubConnection.start().subscribe(object : DisposableCompletableObserver() {
                override fun onComplete() {
                    sendConnectionState()
                }

                override fun onError(e: Throwable) {
                    sendConnectionState()
                    runBlocking(Dispatchers.IO) {
                        delay(retryConnectionTime)
                        startHubConnection()
                    }
                }
            })
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "hubConnection start: ", e)
        }

    }

    fun isConnected(): Boolean {
        return if (this::hubConnection.isInitialized) {
            hubConnection.connectionState == HubConnectionState.CONNECTED
        } else {
            false
        }
    }

    fun sendConnectionState() {
        smilinnoListener?.onConnectionStateChange(hubConnection.connectionState)
    }

    fun sendVoiceChat(voiceBase64String: String) {
        chatStateFlow.value = ApiState.Loading
        if (!isConnected()) {
            runBlocking(Dispatchers.IO) {
                delay(retryConnectionTime)
                sendVoiceChat(voiceBase64String)
            }
        } else {
            try {
                Log.d(TAG, "hubConnection sent!")
                hubConnection.send(VOICEMESSAGE, voiceBase64String)
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "hubConnection send error: ", e)
            }
        }

    }

    fun sendTextChat(text: String) {
        chatStateFlow.value = ApiState.Loading
        if (!isConnected()) {
            runBlocking(Dispatchers.IO) {
                delay(retryConnectionTime)
                sendTextChat(text)
            }
            smilinnoListener?.onMessageError(Exception("Server ${hubConnection.connectionState}"))

        } else {
            try {
                Log.d(TAG, "hubConnection send $TEXTMESSAGE : $text")
                hubConnection.send(TEXTMESSAGE, text)
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "hubConnection send error: ", e)
            }
        }

    }

    private fun getTextFromServer() {
        try {
            hubConnection.on(MESSAGE, { message: String ->
                Log.d(TAG, "hubConnection on $MESSAGE: $message")
                val response = convertJsonStringToObject<MessageResponse>(message)
                response.type = MessageType.MESSAGE
                smilinnoListener?.onMessageReceive(response)
            }, String::class.java)
        } catch (e: java.lang.Exception) {
            smilinnoListener?.onMessageError(e)
            Log.e(TAG, "hubConnection on: ", e)
        }
    }

    private fun getErrorFromServer() {
        try {
            hubConnection.on(ERROR, { message: String ->
                Log.d(TAG, "hubConnection on $ERROR: $message")
                smilinnoListener?.onMessageError(Exception(message))
            }, String::class.java)
        } catch (e: java.lang.Exception) {
            smilinnoListener?.onMessageError(e)
            Log.e(TAG, "hubConnection on: ", e)
        }
    }

    private fun getUnRelatedFromServer() {
        try {
            hubConnection.on(UNRELATED, { message: String ->
                Log.d(TAG, "hubConnection on $UNRELATED: $message")
                    val response = convertJsonStringToObject<MessageResponse>(message)
                    response.type = MessageType.UNRELATED
                    smilinnoListener?.onMessageReceive(response)
                    response.voice?.let { voice ->
                        playTTS(voice)
                    }
            }, String::class.java)
        } catch (e: java.lang.Exception) {
            smilinnoListener?.onMessageError(e)
            Log.e(TAG, "hubConnection on: ", e)
        }
    }

    private fun getMoneyTransferFromServer() {
        try {
            hubConnection.on(MONEYTRANSFER, { message: String ->
                Log.d(TAG, "hubConnection on $MONEYTRANSFER: $message")
                val response = convertJsonStringToObject<MessageResponse>(message)
                response.type = MessageType.MONEYTRANSFER
                smilinnoListener?.onMessageReceive(response)
            }, String::class.java)
        } catch (e: java.lang.Exception) {
            smilinnoListener?.onMessageError(e)
            Log.e(TAG, "hubConnection on: ", e)
        }
    }

    private fun getAccountBalanceFromServer() {
        try {
            hubConnection.on(ACCOUNTBALANCE, { message: String ->
                Log.d(TAG, "hubConnection on $ACCOUNTBALANCE: $message")
                val response = convertJsonStringToObject<MessageResponse>(message)
                response.type = MessageType.ACCOUNTBALANCE
                smilinnoListener?.onMessageReceive(response)
            }, String::class.java)
        } catch (e: java.lang.Exception) {
            smilinnoListener?.onMessageError(e)
            Log.e(TAG, "hubConnection on: ", e)
        }
    }

    private fun getAccountBillFromServer() {
        try {
            hubConnection.on(ACCOUNTBILL, { message: String ->
                Log.d(TAG, "hubConnection on $ACCOUNTBILL: $message")
                val response = convertJsonStringToObject<MessageResponse>(message)
                response.type = MessageType.ACCOUNTBILL
                smilinnoListener?.onMessageReceive(response)
            }, String::class.java)
        } catch (e: java.lang.Exception) {
            smilinnoListener?.onMessageError(e)
            Log.e(TAG, "hubConnection on: ", e)
        }
    }

    private fun getPayingTheBillFromServer() {
        try {
            hubConnection.on(PAYINGTHEBILL, { message: String ->
                Log.d(TAG, "hubConnection on $PAYINGTHEBILL: $message")
                val response = convertJsonStringToObject<MessageResponse>(message)
                response.type = MessageType.PAYINGTHEBILL
                smilinnoListener?.onMessageReceive(response)
            }, String::class.java)
        } catch (e: java.lang.Exception) {
            smilinnoListener?.onMessageError(e)
            Log.e(TAG, "hubConnection on: ", e)
        }
    }

    private fun getTokenErrorFromServer() {
        try {
            hubConnection.on(TOKENERROR, { message: String ->
                Log.d(TAG, "hubConnection on $TOKENERROR: $message")
                smilinnoListener?.onMessageError(Exception(message))
            }, String::class.java)
        } catch (e: java.lang.Exception) {
            smilinnoListener?.onMessageError(e)
            Log.e(TAG, "hubConnection on: ", e)
        }
    }

    private inline fun <reified T> convertJsonStringToObject(jsonString: String): T =
        Gson().fromJson(jsonString, T::class.java)

}
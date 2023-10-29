package com.smilinno.smilinnolibrary.util

import android.util.Log
import com.google.gson.Gson
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.TransportEnum
import com.smilinno.smilinnolibrary.callback.SmilinnoListener
import com.smilinno.smilinnolibrary.model.MessageResponse
import com.smilinno.smilinnolibrary.model.MessageType
import com.smilinno.smilinnolibrary.util.Constants.ACCESS_TOKEN_KEY
import com.smilinno.smilinnolibrary.util.Constants.ACCOUNTBALANCE
import com.smilinno.smilinnolibrary.util.Constants.ACCOUNTBILL
import com.smilinno.smilinnolibrary.util.Constants.ERROR
import com.smilinno.smilinnolibrary.util.Constants.HUB_ADDRESS
import com.smilinno.smilinnolibrary.util.Constants.MESSAGE
import com.smilinno.smilinnolibrary.util.Constants.MONEYTRANSFER
import com.smilinno.smilinnolibrary.util.Constants.PAYINGTHEBILL
import com.smilinno.smilinnolibrary.util.Constants.TEXTMESSAGE
import com.smilinno.smilinnolibrary.util.Constants.TOKENERROR
import com.smilinno.smilinnolibrary.util.Constants.UNRELATED
import com.smilinno.smilinnolibrary.util.Constants.VOICEMESSAGE
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    //Initializes the SignalR hub connection.
    fun initSignalRHubConnection(
        token: String,
        retryConnectionTime: Long?,
        playTTS: (voiceUrl: String) -> Unit
    ) {
        try {
            retryConnectionTime?.let {
                this.retryConnectionTime = it
            }
            this.playTTS = playTTS
            hubConnection = HubConnectionBuilder
                .create(HUB_ADDRESS)
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

    //Starts the hub connection.If the connection fails, it will retry after a delay.
    fun startHubConnection() {

        try {
            hubConnection.start().subscribe(object : DisposableCompletableObserver() {
                override fun onComplete() {
                    sendConnectionState()
                }
                override fun onError(e: Throwable) {
                    sendConnectionState()
                        CoroutineScope(Dispatchers.IO).launch {
                            delay(retryConnectionTime)
                            startHubConnection()
                        }


                }
            })
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "hubConnection start: ", e)
        }

    }

    //Checks if the hub connection is connected.
    fun isConnected(): Boolean {
        return if (this::hubConnection.isInitialized) {
            hubConnection.connectionState == HubConnectionState.CONNECTED
        } else {
            false
        }
    }

    //Sends the connection state to the Smilinno listener.
    fun sendConnectionState() {
        smilinnoListener?.onConnectionStateChange(hubConnection.connectionState)
    }

    //Sends a voice chat message to the server.
    fun sendVoiceChat(voiceBase64String: String) {
        if (!isConnected()) {
            CoroutineScope(Dispatchers.IO).launch {
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

    //Sends a text message to the server
    fun sendTextChat(text: String) {
        if (!isConnected()) {
            CoroutineScope(Dispatchers.IO).launch {
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

    //Gets the text from the server.
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

    //Gets error from server.
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

    //Gets unrelated messages from the server.
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

    //Gets the money transfer from the server.
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

    //Gets the account balance from the server.
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

    //Gets the account bill from the server.
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

    //Gets the paying the bill message from the server.
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

    //Gets the token error from the server.
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

    //Converts a JSON string to an object of type [T].
    private inline fun <reified T> convertJsonStringToObject(jsonString: String): T =
        Gson().fromJson(jsonString, T::class.java)

}
package com.smilinno.smilinnolibrary.util

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.internal.LinkedTreeMap
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.TransportEnum
import com.smilinno.smilinnolibrary.callback.AssistantListener
import com.smilinno.smilinnolibrary.callback.VoiceToTextListener
import com.smilinno.smilinnolibrary.callback.TextToVoiceListener
import com.smilinno.smilinnolibrary.model.MessageResponse
import com.smilinno.smilinnolibrary.model.MessageType
import com.smilinno.smilinnolibrary.model.MessageVoiceToText
import com.smilinno.smilinnolibrary.model.MessageTextToVoice
import com.smilinno.smilinnolibrary.util.Constants.ASSISTANT
import com.smilinno.smilinnolibrary.util.Constants.AUTHORIZATION
import com.smilinno.smilinnolibrary.util.Constants.ERROR
import com.smilinno.smilinnolibrary.util.Constants.HUB_ADDRESS_NEW
import com.smilinno.smilinnolibrary.util.Constants.SPEECHTOTEXT
import com.smilinno.smilinnolibrary.util.Constants.TEXTASSISTANT
import com.smilinno.smilinnolibrary.util.Constants.TEXTTOSPEECH
import com.smilinno.smilinnolibrary.util.Constants.VOICEASSISTANT
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception


internal object HubUtil {
    private lateinit var playTTS: (voiceUrl: String) -> Unit
    private var voiceToTextListener: VoiceToTextListener? = null
    private var textToVoiceListener: TextToVoiceListener? = null
    private val TAG: String = HubUtil::class.java.simpleName
    private lateinit var hubConnection: HubConnection
    private var retryConnectionTime = 1000L
    var assistantListener: AssistantListener? = null
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
                .create(HUB_ADDRESS_NEW)
                .withHeader(AUTHORIZATION, token)
                .shouldSkipNegotiate(true)
                .withHandshakeResponseTimeout(15 * 1000)
                .withTransport(TransportEnum.WEBSOCKETS)
                .build()
            hubConnection.serverTimeout = 30 * 1000
            hubConnection.keepAliveInterval = 10 * 1000
            hubConnection.onClosed {
                Log.e(TAG, "HubUtil hubConnection is closed!", it)
                sendConnectionState()
                startHubConnection()
            }
            getTextFromAssistant()
            getVoiceFromText()
            getTextFromVoice()
            getErrorFromAssistant()
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "HubUtil initSignalRHubConnection exception:", e)
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
            Log.e(TAG, "HubUtil hubConnection start: ", e)
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
        assistantListener?.onConnectionStateChange(hubConnection.connectionState)
    }

    //Sends a voice chat message to the server.
    fun sendVoiceToAssistant(voiceBase64String: String) {
        if (!isConnected()) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(retryConnectionTime)
                sendVoiceToAssistant(voiceBase64String)
            }
        } else {
            try {
                Log.d(TAG, "HubUtil send $VOICEASSISTANT : $voiceBase64String")
                hubConnection.send(VOICEASSISTANT, voiceBase64String)
            } catch (e: java.lang.Exception) {
                assistantListener?.onMessageError(e)
                Log.e(TAG, "HubUtil send: ", e)
            }
        }

    }

    //Sends a text message to the server
    fun sendTextToAssistant(text: String) {
        if (!isConnected()) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(retryConnectionTime)
                sendTextToAssistant(text)
            }
        } else {
            try {
                Log.d(TAG, "HubUtil send $TEXTASSISTANT : $text")
                hubConnection.send(TEXTASSISTANT, text)
            } catch (e: java.lang.Exception) {
                assistantListener?.onMessageError(e)
                Log.e(TAG, "HubUtil send: ", e)
            }
        }

    }

    //Sends the given text to the server to get a voice response.
    fun sendTextToGetVoice(text: String, textToVoiceListener: TextToVoiceListener) {
        this.textToVoiceListener = textToVoiceListener
        if (!isConnected()) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(retryConnectionTime)
                sendTextToGetVoice(text, textToVoiceListener)
            }

        } else {
            try {
                Log.d(TAG, "HubUtil send $TEXTTOSPEECH : $text")
                hubConnection.send(TEXTTOSPEECH, text)
            } catch (e: java.lang.Exception) {
                textToVoiceListener.onError(e)
                Log.e(TAG, "HubUtil send: ", e)
            }
        }

    }

    //Sends a voice base64 string to the server and gets the text back
    fun sendVoiceToGetText(voiceBase64String: String, voiceToTextListener: VoiceToTextListener) {
        this.voiceToTextListener = voiceToTextListener
        if (!isConnected()) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(retryConnectionTime)
                sendVoiceToGetText(voiceBase64String, voiceToTextListener)
            }
        } else {
            try {
                Log.d(TAG, "HubUtil send $SPEECHTOTEXT : $voiceBase64String")
                hubConnection.send(SPEECHTOTEXT, voiceBase64String)
            } catch (e: java.lang.Exception) {
                voiceToTextListener.onError(e)
                Log.e(TAG, "HubUtil send: ", e)
            }
        }

    }

    //Gets the voice from the hub connection
    private fun getVoiceFromText() {
        try {
            hubConnection.on(SPEECHTOTEXT, { message: LinkedTreeMap<String, Any> ->
                Log.d(TAG, "HubUtil on $SPEECHTOTEXT: $message")
                val response: MessageVoiceToText = convertToObject(message)
                voiceToTextListener?.onTextReceive(response)
            }, Any::class.java)
        } catch (e: java.lang.Exception) {
            voiceToTextListener?.onError(e)
            Log.e(TAG, "HubUtil on: ", e)
        }
    }

    // Gets the text from the hub connection.
    private fun getTextFromVoice() {
        try {
            hubConnection.on(TEXTTOSPEECH, { message: LinkedTreeMap<String, Any> ->
                Log.d(TAG, "HubUtil on $TEXTTOSPEECH: $message")
                val response: MessageTextToVoice = convertToObject(message)
                textToVoiceListener?.onVoiceReceive(response)
            }, Any::class.java)
        } catch (e: java.lang.Exception) {
            textToVoiceListener?.onError(e)
            Log.e(TAG, "HubUtil on: ", e)
        }
    }

    //Gets the text from the server.
    private fun getTextFromAssistant() {
        try {
            hubConnection.on(ASSISTANT, { message: LinkedTreeMap<String, Any> ->
                Log.d(TAG, "HubUtil on $ASSISTANT: $message")
                val response: MessageResponse = convertToObject(message)
                response.type = MessageType.ASSISTANT
                assistantListener?.onMessageReceive(response)
            }, Any::class.java)
        } catch (e: java.lang.Exception) {
            assistantListener?.onMessageError(e)
            Log.e(TAG, "HubUtil on: ", e)
        }
    }

    //Gets error from server.
    private fun getErrorFromAssistant() {
        try {
            hubConnection.on(ERROR, { message: LinkedTreeMap<String, Any> ->
                Log.d(TAG, "HubUtil on $ERROR: $message")
                val response: MessageResponse = convertToObject(message)
                response.type = MessageType.ERROR
                assistantListener?.onMessageError(Exception(response.text))
            }, Any::class.java)
        } catch (e: java.lang.Exception) {
            assistantListener?.onMessageError(e)
            Log.e(TAG, "HubUtil on: ", e)
        }
    }

    private inline fun <reified T> convertToObject(message: LinkedTreeMap<String, Any>): T {
        val jsonObject: JsonObject = Gson().toJsonTree(message).getAsJsonObject()
        return Gson().fromJson(jsonObject, T::class.java)
    }
}
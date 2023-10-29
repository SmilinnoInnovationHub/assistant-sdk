package com.smilinno.smilinnolibrary

import android.content.Context
import com.smilinno.smilinnolibrary.callback.PlayerListener
import com.smilinno.smilinnolibrary.callback.SmilinnoListener
import com.smilinno.smilinnolibrary.util.HubUtil
import com.smilinno.smilinnolibrary.util.PlayerUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class AssistantLibrary private constructor(private val builder: Builder) {

    //Sends a voice message to the hub.
    fun sendVoiceMessage(base64Message: String) {
        HubUtil.sendVoiceChat(base64Message)
    }

    // Send a text chat message to the Hub.
    fun setTextMessage(text: String) {
        HubUtil.sendTextChat(text)
    }

    // Set the Smilinno listener to the HubUtil.
    fun setSmilinnoCallBack(smilinnoListener : SmilinnoListener) {
        HubUtil.smilinnoListener = smilinnoListener
    }

    // A variables to store.
    var token: String
    var client_id: String? = null
    var publisher: String = "sdk"
    var ttsEnabled: Boolean = false
    var retryConnection : Long? = null

    class Builder(private val context: Context) {
        private lateinit var token: String
        private var client_id: String? = null
        private var publisher: String = "sdk"
        private var ttsEnabled: Boolean = false
        private var retryConnection : Long? = null


        // Sets the variables to the given value.
        fun setToken(token: String) = apply { this.token = token }
        fun setClientId(clientId: String) = apply { this.client_id = clientId }
        fun setPublisher(publisher: String) = apply { this.publisher = publisher }
        fun setRetryConnectionTime(time : Long) = apply { this.retryConnection = time }
        fun isTtsEnabled(ttsStatus: Boolean) = apply { this.ttsEnabled = ttsStatus }

        // This functions returns the current variables
        fun getToken() = token
        fun getClientId() = client_id
        fun getPublisher() = publisher
        fun getTtsEnabled() = ttsEnabled
        fun getRetryConnectionTime() = retryConnection

        // Initialize the SignalR hub connection with the specified token and retry connection .
        private fun bindSocket() {
            HubUtil.initSignalRHubConnection(token,retryConnection) {
                // If TTS is enabled, play the received message using TTS.
                if (ttsEnabled) {
                    runBlocking(Dispatchers.Main) {
                        PlayerUtil.playTTS(context, it, object : PlayerListener {
                            override fun onStopped() {

                            }
                        })
                    }

                }
            }
            // If the hub connection is not connected, start it.
            if (!HubUtil.isConnected())
                HubUtil.startHubConnection()
        }

        fun build(): AssistantLibrary {
            bindSocket()
            return AssistantLibrary(
                builder = this@Builder,
            )
        }
    }


    init {
        token = builder.getToken()
        client_id = builder.getClientId()
        publisher = builder.getPublisher()
        ttsEnabled = builder.getTtsEnabled()
        retryConnection = builder.getRetryConnectionTime()
    }

}
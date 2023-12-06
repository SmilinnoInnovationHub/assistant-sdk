package com.smilinno.smilinnolibrary

import android.app.Activity
import android.app.Application
import android.content.Context
import com.smilinno.smilinnolibrary.callback.PlayerListener
import com.smilinno.smilinnolibrary.callback.AssistantListener
import com.smilinno.smilinnolibrary.callback.StreamVoiceListener
import com.smilinno.smilinnolibrary.callback.VoiceToTextListener
import com.smilinno.smilinnolibrary.callback.TextToVoiceListener
import com.smilinno.smilinnolibrary.util.HubUtil
import com.smilinno.smilinnolibrary.util.PlayerUtil
import com.smilinno.smilinnolibrary.util.WebSocketClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class AssistantLibrary private constructor(private val builder: Builder) {

    /**
     * Starts a WebSocket connection using the provided [context] and sets the [param] as the StreamVoiceListener.
     *
     * @param context The activity context used to initiate the WebSocket connection.
     * @param param The StreamVoiceListener to be set for handling voice stream events.
     */
    fun startWebSocket(context: Activity, param: StreamVoiceListener) {
        WebSocketClient.connect(token,context)
        WebSocketClient.streamVoiceListener = param
    }

    /**
     * Disconnects the WebSocket connection.
     * This function calls the [WebSocketClient.disconnectWebSocket] method to close the WebSocket connection.
     *
     * @see WebSocketClient.disconnectWebSocket
     */
    fun disconnectWebSocket() {
        WebSocketClient.disconnectWebSocket()
    }

    /**
     * Sends a voice message to the assistant.
     *
     * @param base64Message The base64-encoded voice message.
     */
    fun sendVoiceToAssistant(base64Message: String) {
        HubUtil.sendVoiceToAssistant(base64Message)
    }

    /**
     * Sends a text message to the assistant.
     *
     * @param text The text message to send.
     */
    fun sendTextToAssistant(text: String) {
        HubUtil.sendTextToAssistant(text)
    }

    /**
     * Sends a text to get voice.
     *
     * @param text The text to be sent.
     * @param textToVoiceListener The listener to be notified when the voice is received.
     */
    fun sendTextToGetVoice(text: String, textToVoiceListener: TextToVoiceListener) {
        HubUtil.sendTextToGetVoice(text,textToVoiceListener)
    }

    /**
     * Sends a voice message to the server and gets the text back.
     *
     * @param base64Message The base64 encoded voice message.
     * @param voiceToTextListener The listener to be notified when the text is received.
     */
    fun sendVoiceToGetText(base64Message: String, voiceToTextListener: VoiceToTextListener) {
        HubUtil.sendVoiceToGetText(base64Message, voiceToTextListener)
    }

    /**
     * Checks if the device is connected to the internet.
     *
     * @return `true` if the device is connected to the internet, `false` otherwise.
     */
    fun isConnected() = HubUtil.isConnected()

    /**
     * Sets the assistant listener.
     *
     * @param assistantListener The assistant listener.
     */
    fun setAssistantCallBack(assistantListener : AssistantListener) {
        HubUtil.assistantListener = assistantListener
    }

    /**
     * The token used to authenticate with the server.
     */
    var token: String

    /**
     * The client ID used to identify the client.
     */
    var client_id: String? = null

    /**
     * The publisher name used to identify the publisher.
     */
    var publisher: String = "sdk"

    /**
     * Whether or not TTS is enabled.
     */
    var ttsEnabled: Boolean = false

    /**
     * The time in milliseconds to wait before retrying a connection.
     */
    var retryConnection : Long? = null

    class Builder(private val context: Context) {
        private lateinit var token: String
        private var client_id: String? = null
        private var publisher: String = "sdk"
        private var ttsEnabled: Boolean = false
        private var retryConnection : Long? = null


        /**
         * Sets the token to be used for authentication.
         *
         * @param token The token to be used.
         * @return The current instance of the class.
         */
        fun setToken(token: String) = apply { this.token = token }

        /**
         * Sets the client ID to be used for authentication.
         *
         * @param clientId The client ID to be used.
         * @return The current instance of the class.
         */
        fun setClientId(clientId: String) = apply { this.client_id = clientId }

        /**
         * Sets the publisher to be used for authentication.
         *
         * @param publisher The publisher to be used.
         * @return The current instance of the class.
         */
        fun setPublisher(publisher: String) = apply { this.publisher = publisher }

        /**
         * Sets the time to wait before retrying a connection.
         *
         * @param time The time to wait in milliseconds.
         * @return The current instance of the class.
         */
        fun setRetryConnectionTime(time : Long) = apply { this.retryConnection = time }

        /**
         * Enables or disables TTS.
         *
         * @param ttsStatus True to enable TTS, false to disable it.
         * @return The current instance of the class.
         */
        fun isTtsEnabled(ttsStatus: Boolean) = apply { this.ttsEnabled = ttsStatus }

        /**
         * Gets the token.
         *
         * @return The token.
         */
        fun getToken() = token

        /**
         * Gets the client ID.
         *
         * @return The client ID.
         */
        fun getClientId() = client_id

        /**
         * Gets the publisher.
         *
         * @return The publisher.
         */
        fun getPublisher() = publisher

        /**
         * Gets the TTS enabled flag.
         *
         * @return The TTS enabled flag.
         */
        fun getTtsEnabled() = ttsEnabled

        /**
         * Gets the retry connection time.
         *
         * @return The retry connection time.
         */
        fun getRetryConnectionTime() = retryConnection

        /**
         * Initialize the SignalR hub connection with the specified token and retry connection .
         */
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
            /**
             * If the hub connection is not connected, start it.
             */
            if (!HubUtil.isConnected())
                HubUtil.startHubConnection()
        }

        /**
         * Builds an AssistantLibrary.
         *
         * @return The AssistantLibrary.
         */
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
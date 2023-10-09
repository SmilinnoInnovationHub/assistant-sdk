package com.smilinno.smilinnolibrary

import android.content.Context
import com.smilinno.smilinnolibrary.callback.SmilinnoListener
import com.smilinno.smilinnolibrary.util.HubUtil

class SmilinnoLibrary private constructor(private val builder: Builder) {
    fun sendVoiceMessage(base64Message: String) {
        HubUtil.sendVoiceChat(base64Message)
    }

    fun setTextMessage(text: String) {
        HubUtil.sendTextChat(text)
    }

    fun setSmilinnoCallBack(smilinnoListener : SmilinnoListener) {
        HubUtil.smilinnoListener = smilinnoListener
    }

    var token: String
    var client_id: String? = null
    var publisher: String = "sdk"
    var ttsEnabled: Boolean = false


    class Builder(private val context: Context) {
        private lateinit var token: String
        private var client_id: String? = null
        private var publisher: String = "sdk"
        private var ttsEnabled: Boolean = false

        fun setToken(token: String) = apply { this.token = token }
        fun setClientId(clientId: String) = apply { this.client_id = clientId }
        fun setPublisher(publisher: String) = apply { this.publisher = publisher }
        fun isTtsEnabled(ttsStatus: Boolean) = apply { this.ttsEnabled = ttsStatus }
        fun getToken() = token
        fun getClientId() = client_id
        fun getPublisher() = publisher
        fun getTtsEnabled() = ttsEnabled

        private fun bindSocket() {
            HubUtil.initSignalRHubConnection(getToken())
            if (!HubUtil.isConnected())
                HubUtil.startHubConnection()
        }

        fun build(): SmilinnoLibrary {
            bindSocket()
            return SmilinnoLibrary(
                builder = this@Builder,
            )
        }
    }


    init {
        token = builder.getToken()
        client_id = builder.getClientId()
        publisher = builder.getPublisher()
        ttsEnabled = builder.getTtsEnabled()
    }

}
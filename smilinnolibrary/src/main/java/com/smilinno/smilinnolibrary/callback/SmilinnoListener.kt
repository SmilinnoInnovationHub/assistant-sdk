package com.smilinno.smilinnolibrary.callback

import com.microsoft.signalr.HubConnectionState
import com.smilinno.smilinnolibrary.model.MessageResponse

interface SmilinnoListener {
    fun onMessageReceive(message: MessageResponse)
    fun onMessageError(e : Exception)
    fun onConnectionStateChange(connectionState: HubConnectionState)

}
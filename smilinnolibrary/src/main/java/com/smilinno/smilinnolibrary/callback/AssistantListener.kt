package com.smilinno.smilinnolibrary.callback

import com.microsoft.signalr.HubConnectionState
import com.smilinno.smilinnolibrary.model.MessageResponse

/**
 * An interface that defines the methods that an assistant listener must implement.
 */
interface AssistantListener {
    fun onMessageReceive(message: MessageResponse)
    fun onMessageError(e : Exception)
    fun onConnectionStateChange(connectionState: HubConnectionState)

}
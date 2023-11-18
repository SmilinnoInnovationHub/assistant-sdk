package com.smilinno.smilinnolibrary.callback

import com.microsoft.signalr.HubConnectionState
import com.smilinno.smilinnolibrary.model.MessageResponse

//Interface that should be implemented by any class that wants to listen to Smilinno events.
interface AssistantListener {
    fun onMessageReceive(message: MessageResponse)
    fun onMessageError(e : Exception)
    fun onConnectionStateChange(connectionState: HubConnectionState)

}
package com.smilinno.smilinnolibrary.callback

import com.microsoft.signalr.HubConnectionState

interface SmilinnoListener {
    fun onMessageReceive(message: String)
    fun onMessageError(e : Exception)
    fun onConnectionStateChange(connectionState: HubConnectionState)

}
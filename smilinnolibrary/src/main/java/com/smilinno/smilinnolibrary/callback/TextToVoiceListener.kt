package com.smilinno.smilinnolibrary.callback

import com.smilinno.smilinnolibrary.model.MessageTextToVoice

//Interface that should be implemented by any class that wants to listen to Smilinno events.
interface TextToVoiceListener {
    fun onVoiceReceive(message: MessageTextToVoice)
    fun onError(e : Exception)

}
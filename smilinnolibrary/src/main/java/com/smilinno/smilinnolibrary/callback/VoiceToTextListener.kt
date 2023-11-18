package com.smilinno.smilinnolibrary.callback

import com.smilinno.smilinnolibrary.model.MessageVoiceToText

//Interface that should be implemented by any class that wants to listen to Smilinno events.
interface VoiceToTextListener {
    fun onTextReceive(message: MessageVoiceToText)
    fun onError(e : Exception)

}
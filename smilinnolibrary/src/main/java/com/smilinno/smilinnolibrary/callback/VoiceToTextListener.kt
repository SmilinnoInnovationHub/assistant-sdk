package com.smilinno.smilinnolibrary.callback

import com.smilinno.smilinnolibrary.model.MessageVoiceToText

/**
 * Interface for listening to voice to text events.
 */
interface VoiceToTextListener {
    fun onTextReceive(message: MessageVoiceToText)
    fun onError(e : Exception)

}
package com.smilinno.smilinnolibrary.callback

import com.smilinno.smilinnolibrary.model.MessageTextToVoice

/**
 * Interface for listening to text-to-voice events.
 */
interface TextToVoiceListener {
    fun onVoiceReceive(message: MessageTextToVoice)
    fun onError(e : Exception)
}
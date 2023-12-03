package com.smilinno.smilinnolibrary.callback

/**
 * An interface that defines the methods that an assistant listener must implement.
 */
interface StreamVoiceListener {
    fun onReadyForSpeech()
    fun onEndOfSpeech(reason: String)
    fun onError(e : Throwable)
    fun onResults(text: String)
    fun onPartialResults(hex: String)

}
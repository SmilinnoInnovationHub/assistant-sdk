package com.smilinno.smilinnolibrary.model

import com.google.gson.annotations.SerializedName

/**
 * Data class for MessageTextToVoice.
 * @property voice The voice to use for the text to voice message.
 */
data class MessageTextToVoice(
    @SerializedName("voice")
    val voice: String?,
)
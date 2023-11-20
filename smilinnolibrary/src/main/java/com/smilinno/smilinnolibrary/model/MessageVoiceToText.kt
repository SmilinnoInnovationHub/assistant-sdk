package com.smilinno.smilinnolibrary.model

import com.google.gson.annotations.SerializedName

/**
 * A data class that represents a message that was sent as a voice message and was converted to text.
 * @property text The text that was converted from the voice message.
 */
data class MessageVoiceToText(
    @SerializedName("text")
    val text: String?,
)
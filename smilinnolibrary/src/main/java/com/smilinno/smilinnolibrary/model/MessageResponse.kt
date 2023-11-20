package com.smilinno.smilinnolibrary.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

/**
 * A data class that represents a message response.
 *
 * @property text The text of the message.
 * @property voice The voice of the message.
 * @property custom Any custom data associated with the message.
 * @property type The type of the message.
 */
@Keep
data class MessageResponse(
    @SerializedName("text")
    val text: String?,
    @SerializedName("voice")
    val voice: String?,
    @SerializedName("custom")
    val custom: Any?,
    var type : MessageType?,
)
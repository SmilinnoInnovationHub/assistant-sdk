package com.smilinno.smilinnolibrary.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

//Data class that represents a message response.
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
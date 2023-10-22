package com.smilinno.smilinnolibrary.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class MessageResponse(
    @SerializedName("text")
    val text: String?,
    @SerializedName("voice")
    val voice: String?,
    var type : MessageType?,
)
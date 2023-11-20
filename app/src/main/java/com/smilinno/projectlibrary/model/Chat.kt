package com.smilinno.projectlibrary.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.smilinno.smilinnolibrary.model.MessageType

@Keep
data class Chat(
    val text: String?,
    val voice: String? = null,
    val isAssistant: Boolean = false,
    val custom: Any?,
    var type : MessageType?,

    )
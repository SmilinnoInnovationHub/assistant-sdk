package com.smilinno.smilinnolibrary.model

import com.google.gson.annotations.SerializedName

data class MessageVoiceToText(
    @SerializedName("text")
    val text: String?,
)
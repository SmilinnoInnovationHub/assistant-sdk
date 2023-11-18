package com.smilinno.smilinnolibrary.model

import com.google.gson.annotations.SerializedName

data class MessageTextToVoice(
    @SerializedName("voice")
    val voice: String?,
)
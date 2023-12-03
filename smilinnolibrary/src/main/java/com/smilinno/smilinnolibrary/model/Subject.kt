package com.smilinno.smilinnolibrary.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class Subject(
    @SerializedName("max-binary-msg-buffer-size")
    val maxBinaryMsgBufferSize: Int?, // 64000
    @SerializedName("result")
    val result: Int?, // 0
    @SerializedName("subject")
    val subject: String? // INIT
)
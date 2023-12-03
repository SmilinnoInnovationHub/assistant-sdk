package com.smilinno.smilinnolibrary.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class SubjectReQ(
    @SerializedName("decoding-info")
    val decodingInfo: DecodingInfo?,
    @SerializedName("engine")
    val engine: String?, // 1
    @SerializedName("guid")
    val guid: String?,
    @SerializedName("mode")
    val mode: String?, // stream-custom-raw
    @SerializedName("subject")
    val subject: String? // REQ
) {
    @Keep
    data class DecodingInfo(
        @SerializedName("ref-text")
        val refText: String?,
        @SerializedName("sample-rate")
        val sampleRate: Int? // 16000
    )
}
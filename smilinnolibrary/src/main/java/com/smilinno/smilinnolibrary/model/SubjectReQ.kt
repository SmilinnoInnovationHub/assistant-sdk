package com.smilinno.smilinnolibrary.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

/**
 * Data class representing a Subject Request.
 *
 * This class is annotated with @Keep to indicate that it should not be removed by code shrinking tools.
 *
 * @property decodingInfo Information about decoding.
 * @property engine The engine associated with the subject request.
 * @property guid The GUID (Globally Unique Identifier) associated with the subject request.
 * @property mode The mode of the subject request (e.g., "stream-custom-raw").
 * @property subject The subject of the request (e.g., "REQ").
 */
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
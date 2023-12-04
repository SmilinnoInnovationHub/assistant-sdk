package com.smilinno.smilinnolibrary.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class TextPartial(
    @SerializedName("final")
    val `final`: Boolean?, // false
    @SerializedName("transcription")
    val transcription: Transcription?
) {
    @Keep
    data class Transcription(
        @SerializedName("partial")
        val partial: String? // سلام
    )
}
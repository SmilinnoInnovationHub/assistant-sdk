package com.smilinno.smilinnolibrary.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class TextResult(
    @SerializedName("final")
    val `final`: Boolean?, // false
    @SerializedName("transcription")
    val transcription: Transcription?
) {
    @Keep
    data class Transcription(
        @SerializedName("result")
        val result: List<Result>?,
        @SerializedName("text")
        val text: String? // سلام
    ) {
        @Keep
        data class Result(
            @SerializedName("conf")
            val conf: Double?, // 1.0
            @SerializedName("end")
            val end: Double?, // 2.4
            @SerializedName("space_after")
            val spaceAfter: Boolean?, // true
            @SerializedName("start")
            val start: Double?, // 0.0
            @SerializedName("word")
            val word: String? // سلام
        )
    }
}
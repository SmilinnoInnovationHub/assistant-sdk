package com.smilinno.smilinnolibrary.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

/**
 * Data class representing a TextResult, which contains information about a transcription result.
 * This class is annotated with `@Keep` to indicate that the class and its members should be
 * kept during code shrinking and obfuscation.
 *
 * @property final Indicates whether the transcription is final or not. Default is `false`.
 * @property transcription The transcription details, represented by the [Transcription] data class.
 */
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
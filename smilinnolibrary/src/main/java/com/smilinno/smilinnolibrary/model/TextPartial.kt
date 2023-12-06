package com.smilinno.smilinnolibrary.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

/**
 * Data class representing a partial text with transcription information.
 *
 * Use [TextPartial.Transcription] to access the transcription details.
 *
 * @property `final` Indicates whether the text is final or not. Defaults to `false`.
 * @property transcription The transcription details.
 *
 * @see TextPartial.Transcription
 */
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
package com.smilinno.smilinnolibrary.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

/**
 * Data class representing a SubjectSoeS object.
 *
 * This class is annotated with @Keep to ensure that ProGuard doesn't remove it during code shrinking.
 *
 * @property subject The subject associated with the SubjectSoeS object. It represents the SOES.
 */
@Keep
data class SubjectSoeS(
    @SerializedName("subject")
    val subject: String? // SOES
)
package com.smilinno.smilinnolibrary.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

/**
 * Represents a SubjectEOES (Subject for EOES).
 *
 * This data class is used to store information about a subject related to EOES.
 *
 * @property subject The subject associated with EOES. It can be null if not available.
 *                   Default value is "SOES" when not provided.
 */
@Keep
data class SubjectEOES(
    @SerializedName("subject")
    val subject: String? // SOES
)
package com.smilinno.smilinnolibrary.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class SubjectSoeS(
    @SerializedName("subject")
    val subject: String? // SOES
)
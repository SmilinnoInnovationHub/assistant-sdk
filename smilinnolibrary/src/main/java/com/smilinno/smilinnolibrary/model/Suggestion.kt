package com.smilinno.smilinnolibrary.model


import androidx.annotation.Keep

@Keep
internal data class Suggestion(
    val id: String?,
    val title: String?,
    val needMessage: Boolean?,
    )
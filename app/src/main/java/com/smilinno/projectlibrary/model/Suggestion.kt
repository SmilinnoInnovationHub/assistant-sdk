package com.smilinno.projectlibrary.model


import androidx.annotation.Keep

@Keep
 data class Suggestion(
    val id: String?,
    val title: String?,
    val needMessage: Boolean?,
    )
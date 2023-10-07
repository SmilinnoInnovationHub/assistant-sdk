package com.smilinno.smilinnolibrary.model

import androidx.annotation.Keep

@Keep
internal data class CreatedDate(
    val raw: String? = null,
    var displayDate: String? = null,
    var time: String? = null
)
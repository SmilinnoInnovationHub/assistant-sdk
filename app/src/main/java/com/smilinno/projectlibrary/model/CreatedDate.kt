package com.smilinno.projectlibrary.model

import androidx.annotation.Keep

@Keep
 data class CreatedDate(
    val raw: String? = null,
    var displayDate: String? = null,
    var time: String? = null
)
package com.smilinno.smilinnolibrary.model

import androidx.annotation.Keep

@Keep
internal data class Feedback(
    var status: String? = null,
    var badgeMessage: String? = null
)
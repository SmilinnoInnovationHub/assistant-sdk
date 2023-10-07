package com.smilinno.projectlibrary.model

import androidx.annotation.Keep

@Keep
 data class Feedback(
    var status: String? = null,
    var badgeMessage: String? = null
)
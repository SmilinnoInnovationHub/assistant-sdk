package com.smilinno.smilinnolibrary.model

import androidx.annotation.Keep

@Keep
internal class Link(
    var rel: String? = null,
    var method: String? = null,
    var href: String? = null
)
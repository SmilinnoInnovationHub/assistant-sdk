package com.smilinno.projectlibrary.model

import androidx.annotation.Keep

@Keep
 class Link(
    var rel: String? = null,
    var method: String? = null,
    var href: String? = null
)
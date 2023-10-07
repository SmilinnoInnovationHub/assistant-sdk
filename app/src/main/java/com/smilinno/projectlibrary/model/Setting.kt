package com.smilinno.projectlibrary.model

import androidx.annotation.Keep

@Keep
 class Setting(
    var policy: String? = null,
    var about: String? = null,
    var googleAsr: Boolean? = null,
    var chatTimeOutSeconds: Int? = null,
    var voiceLengthSeconds: Int? = null,
    )
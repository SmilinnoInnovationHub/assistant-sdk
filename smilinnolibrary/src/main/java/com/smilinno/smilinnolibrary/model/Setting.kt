package com.smilinno.smilinnolibrary.model

import androidx.annotation.Keep

@Keep
internal class Setting(
    var policy: String? = null,
    var about: String? = null,
    var googleAsr: Boolean? = null,
    var chatTimeOutSeconds: Int? = null,
    var voiceLengthSeconds: Int? = null,
    )
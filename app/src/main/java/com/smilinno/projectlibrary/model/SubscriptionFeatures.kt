package com.smilinno.projectlibrary.model

import androidx.annotation.Keep

@Keep
 data class SubscriptionFeatures(

    val featureType: String,

    val count: String,

    val infinity: Boolean
)
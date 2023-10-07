package com.smilinno.smilinnolibrary.model

import androidx.annotation.Keep

@Keep
internal data class SubscriptionFeatures(

    val featureType: String,

    val count: String,

    val infinity: Boolean
)
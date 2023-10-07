package com.smilinno.smilinnolibrary.model

import androidx.annotation.Keep

@Keep
internal data class SubscriptionProfile(

    var totalCoin: String,

    val features: ArrayList<SubscriptionFeatures>,

    val description: String
)
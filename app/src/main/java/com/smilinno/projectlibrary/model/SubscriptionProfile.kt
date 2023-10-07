package com.smilinno.projectlibrary.model

import androidx.annotation.Keep
import com.smilinno.projectlibrary.model.SubscriptionFeatures

@Keep
 data class SubscriptionProfile(

    var totalCoin: String,

    val features: ArrayList<SubscriptionFeatures>,

    val description: String
)
package com.smilinno.smilinnolibrary.model

import androidx.annotation.Keep

@Keep
internal data class ResponseChatId (
    var requestId: String? = null,
    var chatId: String? = null,
)
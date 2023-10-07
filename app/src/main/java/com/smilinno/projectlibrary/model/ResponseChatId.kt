package com.smilinno.projectlibrary.model

import androidx.annotation.Keep

@Keep
 data class ResponseChatId (
    var requestId: String? = null,
    var chatId: String? = null,
)
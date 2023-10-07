package com.smilinno.projectlibrary.model


import androidx.annotation.Keep

@Keep
 data class FeedbackDetail(
    val chatId: String?,
    val hasMore: Boolean?,
    val hint: Any?,
    val links: List<Link?>?,
    val suggestions: List<Suggestion>?,
    val title: Any?
)
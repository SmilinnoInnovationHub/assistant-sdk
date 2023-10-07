package com.smilinno.smilinnolibrary.model

import androidx.annotation.Keep
import com.smilinno.smilinnolibrary.model.type.DeliverType
import com.smilinno.smilinnolibrary.model.type.ChatType

@Keep
internal data class Chat(
    var id: String? = null,
    var text: String? = null,
    var time: Long = 0,
    var isAssistant: Boolean = false,
    var deliverType: DeliverType = DeliverType.DELIVER,
    var isError: Boolean = false,
    var finished : Boolean = true,
    var type: String? = null,
    var voice: String? = null,
    var clientRequestId: String? = null,
    var createdBy: String? = null,
    var responseType: ChatType = ChatType.Text,
    var imageUrls: ArrayList<String>? = null,
    var voiceBytes: ByteArray? = null,
    var voiceBase64String: String? = null,
    var voiceUrl: String? = null,
    var shareText: String? = null,
    var createdOn: CreatedDate? = null,
    var subscription : SubscriptionProfile? = null,
    var feedback: Feedback? = null,
    var links: List<Link>? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chat

        if (id != other.id) return false
        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }
}
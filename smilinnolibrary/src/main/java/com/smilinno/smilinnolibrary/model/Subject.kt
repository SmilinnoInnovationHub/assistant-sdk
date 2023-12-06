package com.smilinno.smilinnolibrary.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

/**
 * Represents a subject entity with attributes related to binary message buffer size,
 * result code, and subject string.
 *
 * @property maxBinaryMsgBufferSize The maximum size of the binary message buffer.
 *                                  Default value is 64000.
 * @property result The result code associated with the subject. Default value is 0.
 * @property subject The subject string. Default value is "INIT".
 */
@Keep
data class Subject(
    @SerializedName("max-binary-msg-buffer-size")
    val maxBinaryMsgBufferSize: Int?, // 64000
    @SerializedName("result")
    val result: Int?, // 0
    @SerializedName("subject")
    val subject: String? // INIT
)
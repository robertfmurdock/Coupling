package com.zegreatrob.coupling.server.slack

import node.buffer.BufferEncoding
import node.crypto.createHmac

class SlackRequestVerifier(private val signingSecret: String) {

    fun signature(requestTimestamp: Int, body: String) = listOf("v0", requestTimestamp, body)
        .joinToString(":")
        .hmac256Hash()
        .let { "v0=$it" }

    private fun String.hmac256Hash() = createHmac("sha256", signingSecret).update(this)
        .digest()
        .toString(BufferEncoding.hex)
}

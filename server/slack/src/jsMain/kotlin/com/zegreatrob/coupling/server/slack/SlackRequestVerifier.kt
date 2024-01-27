package com.zegreatrob.coupling.server.slack

import node.buffer.BufferEncoding

// import node.crypto.Hmac
// import node.crypto.createHmac

class SlackRequestVerifier(private val signingSecret: String) {

    fun signature(requestTimestamp: Int, body: String) = listOf("v0", requestTimestamp, body)
        .joinToString(":")
        .hmac256Hash()
        .let { "v0=$it" }

    private fun String.hmac256Hash() = createHmac("sha256", signingSecret).update(this)
        .digest()
        .toString(BufferEncoding.hex)

    private fun createHmac(s: String, signingSecret: String): Hmac = kotlinext.js.require<dynamic>("crypto")
        .createHmac(s, signingSecret)
}

external interface Hmac {
    fun update(s: String): Hmac
    fun digest(): Hmac
    fun toString(hex: BufferEncoding): String
}

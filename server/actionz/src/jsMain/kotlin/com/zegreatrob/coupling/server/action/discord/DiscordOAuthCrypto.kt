package com.zegreatrob.coupling.server.action.discord

import node.buffer.BufferEncoding
import node.buffer.hex
import node.crypto.createHash
import node.crypto.randomBytes

fun discordOAuthHash(value: String): String = createHash("sha256").update(value).digest().toString(BufferEncoding.hex)
fun newDiscordOAuthState(): String = randomBytes(32).toString(BufferEncoding.hex)

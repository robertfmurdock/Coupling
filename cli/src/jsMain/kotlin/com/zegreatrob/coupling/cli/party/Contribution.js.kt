package com.zegreatrob.coupling.cli.party

import node.buffer.Buffer
import node.buffer.BufferEncoding
import node.fs.readSync

actual fun readLineFromStandardIn(): String = try {
    buildString {
        var char = ""
        val buf = Buffer.alloc(1)
        do {
            val bytesRead = readSync(fd = 0, buffer = buf, offset = 0, length = 1, position = null)
            if (bytesRead.toInt() > 0) {
                char = buf.toString(encoding = BufferEncoding.utf8)
                append(char)
            }
        } while (char != "\n")
    }
} catch (e: Exception) {
    null
} ?: ""

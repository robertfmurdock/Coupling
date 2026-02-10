package com.zegreatrob.coupling.cli

import node.buffer.BufferEncoding
import node.buffer.utf8
import node.fs.existsSync
import node.fs.readFileSync

actual fun readFromFile(fileName: String): String? = if (existsSync(fileName)) {
    readFileSync(fileName, BufferEncoding.utf8)
} else {
    null
}

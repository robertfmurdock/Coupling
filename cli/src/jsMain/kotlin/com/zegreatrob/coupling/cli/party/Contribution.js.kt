package com.zegreatrob.coupling.cli.party

import node.buffer.BufferEncoding
import node.fs.existsSync
import node.fs.readFileSync

actual fun loadFile(path: String): String? = if (existsSync(path)) {
    readFileSync(path, BufferEncoding.utf8)
} else {
    null
}

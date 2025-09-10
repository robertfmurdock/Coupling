package com.zegreatrob.coupling.action

import node.buffer.BufferEncoding
import node.fs.readFileSync

actual inline fun <reified T> loadResource(fileResource: String): T = JSON.parse(readFileSync("./kotlin/$fileResource", BufferEncoding.utf8))

@file:JsModule("stream")

package com.zegreatrob.coupling.server.external.stream

import com.zegreatrob.coupling.server.external.fs.Stream

external object Readable {
    fun from(input: Array<String>): Stream
}

package com.zegreatrob.coupling.e2e.external.fsextras

import com.zegreatrob.coupling.e2e.external.childprocess.Writable
import kotlin.js.Json

@JsModule("fs-extra")
external val fs: FilesystemExtra

external interface FilesystemExtra {
    fun mkdirSync(path: String, options: Json)
    fun createWriteStream(filePath: String): Writable
    fun removeSync(path: String)
}

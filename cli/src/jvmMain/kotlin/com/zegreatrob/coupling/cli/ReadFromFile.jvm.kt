package com.zegreatrob.coupling.cli

import java.io.File

actual fun readFromFile(fileName: String): String? {
    val file = File(fileName)
    return if (file.isFile) {
        file.readText(Charsets.UTF_8)
    } else {
        null
    }
}

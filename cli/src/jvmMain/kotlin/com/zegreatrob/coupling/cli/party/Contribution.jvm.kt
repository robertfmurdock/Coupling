package com.zegreatrob.coupling.cli.party

import java.io.File

actual fun loadFile(path: String): String? {
    val file = File(path)
    return if (file.isFile) {
        file.readLines().joinToString("\n")
    } else {
        null
    }
}

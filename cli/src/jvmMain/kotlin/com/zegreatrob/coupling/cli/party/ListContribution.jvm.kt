package com.zegreatrob.coupling.cli.party

import java.io.File

actual fun writeFile(path: String, content: String) {
    File(path).writeText(content)
}

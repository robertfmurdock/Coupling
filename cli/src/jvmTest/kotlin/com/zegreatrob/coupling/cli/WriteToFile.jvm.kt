package com.zegreatrob.coupling.cli

import java.io.File

actual fun String.writeToFile(outputFile: String) = File(outputFile).writeText(this)

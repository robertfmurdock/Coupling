package com.zegreatrob.coupling.cli

import node.fs.writeFileSync

actual fun String.writeToFile(outputFile: String): Unit = writeFileSync(outputFile, this)

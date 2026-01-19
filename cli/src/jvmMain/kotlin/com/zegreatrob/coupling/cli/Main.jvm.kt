package com.zegreatrob.coupling.cli

import java.io.File

actual fun getEnv(variableName: String): String? = System.getenv(variableName)
actual fun readFileText(filePath: String): String? = File(filePath).run {
    if (exists()) {
        readText()
    } else {
        null
    }
}

actual fun makeDirectory(couplingHomeDirectory: String) {
    File(couplingHomeDirectory).mkdirs()
}

actual fun writeDataToFile(configFilePath: String, text: String) {
    File(configFilePath).writeText(text)
}

actual fun platformArgCorrection(args: Array<String>): Array<String> = args

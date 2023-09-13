package com.zegreatrob.coupling.cli

import node.fs.mkdirSync
import node.fs.readFileSync
import node.fs.writeFileSync
import node.process.process

actual fun getEnv(variableName: String): String? = process.env[variableName]
actual fun readFileText(filePath: String): String = readFileSync(filePath).toString()
actual fun makeDirectory(couplingHomeDirectory: String) {
    mkdirSync(couplingHomeDirectory)
}

actual fun writeDataToFile(configFilePath: String, text: String) {
    writeFileSync(configFilePath, text)
}

package com.zegreatrob.coupling.cli

import node.buffer.BufferEncoding
import node.fs.existsSync
import node.fs.mkdirSync
import node.fs.readFileSync
import node.fs.writeFileSync
import node.process.process

actual fun getEnv(variableName: String): String? = process.env[variableName]
actual fun readFileText(filePath: String): String = readFileSync(filePath).toString(encoding = BufferEncoding.utf8)
actual fun makeDirectory(couplingHomeDirectory: String) {
    if (!existsSync(couplingHomeDirectory)) {
        mkdirSync(couplingHomeDirectory)
    }
}

actual fun writeDataToFile(configFilePath: String, text: String) {
    writeFileSync(configFilePath, text)
}

actual fun platformArgCorrection(args: Array<String>) = process.argv.slice(2..<process.argv.size).toTypedArray()

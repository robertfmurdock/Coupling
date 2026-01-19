package com.zegreatrob.coupling.cli

import js.objects.unsafeJso
import node.buffer.BufferEncoding
import node.fs.MkdirSyncOptions
import node.fs.existsSync
import node.fs.mkdirSync
import node.fs.readFileSync
import node.fs.writeFileSync
import node.process.process

actual fun getEnv(variableName: String): String? = process.env[variableName]
actual fun readFileText(filePath: String): String? = if (existsSync(filePath)) {
    readFileSync(filePath, BufferEncoding.utf8)
} else {
    null
}

actual fun makeDirectory(couplingHomeDirectory: String) {
    if (!existsSync(couplingHomeDirectory)) {
        mkdirSync(couplingHomeDirectory, unsafeJso<MkdirSyncOptions> { })
    }
}

actual fun writeDataToFile(configFilePath: String, text: String) {
    writeFileSync(configFilePath, text)
}

actual fun platformArgCorrection(args: Array<String>) = process.argv.slice(2..<process.argv.size).toTypedArray()

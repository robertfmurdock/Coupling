package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.e2e.external.childprocess.ChildProcess
import com.zegreatrob.coupling.e2e.external.childprocess.fork
import com.zegreatrob.coupling.e2e.external.fsextras.fs
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.json

suspend fun startServer(): ChildProcess {
    val command = process.env["APP_PATH"]?.toString() ?: throw Exception("No APP_PATH")
    val split = command.split(" ")
    val module = split[0]
    val arguments = split.subList(1, split.size)
    return fork(
        module,
        arguments.toTypedArray(),
        json("stdio" to "pipe")
    ).also {
        Promise<Unit> { resolve, reject ->
            connectToServerProcess(it, resolve, reject)
        }.await()
    }
}

private fun connectToServerProcess(
    serverProcess: ChildProcess,
    resolve: (Unit) -> Unit,
    reject: (Throwable) -> Unit
) {
    serverProcess.on("message") { message: String ->
        if (message == "ready") {
            console.log("server ready")
            resolve(Unit)
        }
    }
    serverProcess.on("exit") { message: String ->
        console.log("server exit", message)
        reject(Exception(message))
    }

    serverProcess.stdout.on("data") { buffer ->
        val message = buffer.toString("utf8")
        if (message.toString().contains("ready")) {
            console.log("server ready")
            resolve(Unit)
        }
    }

    process.stdin.pipe(serverProcess.stdin)

    val logsDir = process.env["LOGS_DIR"]
    fs.mkdirSync("$logsDir", json("recursive" to true))
    val serverOut = fs.createWriteStream("$logsDir/server.out.log")
    val serverErr = fs.createWriteStream("$logsDir/server.err.log")
    serverProcess.stdout.pipe(serverOut)
    serverProcess.stderr.pipe(serverErr)
}

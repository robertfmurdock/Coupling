package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.e2e.external.childprocess.ChildProcess
import com.zegreatrob.coupling.e2e.external.childprocess.fork
import com.zegreatrob.coupling.e2e.external.fsextras.fs
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.json

suspend fun startServer() = fork(
    "../server/build/executable/app",
    emptyArray(),
    json("stdio" to "pipe")
)
    .also {
        Promise<Unit> { resolve, reject ->
            connectToServerProcess(
                it,
                resolve,
                reject
            )
        }.await()
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

    process.stdin.pipe(serverProcess.stdin);

    fs.mkdirSync("./build/logs", json("recursive" to true));
    val serverOut = fs.createWriteStream("./build/logs/server.out.log")
    val serverErr = fs.createWriteStream("./build/logs/server.err.log")
    serverProcess.stdout.pipe(serverOut)
    serverProcess.stderr.pipe(serverErr)
}

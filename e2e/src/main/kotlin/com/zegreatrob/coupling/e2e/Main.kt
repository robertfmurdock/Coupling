package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.e2e.external.childprocess.ChildProcess
import com.zegreatrob.coupling.e2e.external.childprocess.Writable
import com.zegreatrob.coupling.e2e.external.childprocess.fork
import com.zegreatrob.coupling.e2e.external.wdio.Launcher
import com.zegreatrob.coupling.e2e.external.webpack.WebpackConfig
import com.zegreatrob.coupling.e2e.external.webpack.webpack
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

fun main() {
    console.log("Main E2E Begin.")
    js("process.env.PORT = 3001")
    js("console.log(__dirname)")

    val serverProcess = fork("../server/build/executable/app", emptyArray(), json("stdio" to "pipe"))

    Promise.all(
        arrayOf(
            Promise { resolve, reject -> connectToServerProcess(serverProcess, resolve, reject) },
            runWebpack(kotlinext.js.require("webpack.config.js").unsafeCast<WebpackConfig>())
        )
    ).then {
        console.log("do tests here")
        Launcher("./build/.tmp/config2.js", json())
            .run()
    }.finally {
        fs.removeSync("./build/.tmp")
    }.then { result ->
        console.log("Slay queen", result)
        serverProcess.kill()
        process.exit(result)
    }.catch {
        console.log("Error", it)
        process.exit(-1)
    }
}

private fun <T> Promise<T>.finally(work: () -> Unit) = then({ work(); it }, { work(); throw it })

fun runWebpack(config: WebpackConfig): Promise<Unit> = Promise { resolve, reject ->
    webpack(config)
        .run { err: Throwable?, stats ->
            console.log(stats.toString().unsafeCast<String>())
            if (err != null) {
                reject(err)
            }
            console.log("Starting tests:")
            resolve(Unit)
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

    process.stdin.pipe(serverProcess.stdin);

    fs.mkdirSync("./build/logs", json("recursive" to true));
    val serverOut = fs.createWriteStream("./build/logs/server.out.log")
    val serverErr = fs.createWriteStream("./build/logs/server.err.log")
    serverProcess.stdout.pipe(serverOut)
    serverProcess.stderr.pipe(serverErr)
}

@JsModule("fs-extra")
external val fs: FilesystemExtra

external interface FilesystemExtra {
    fun mkdirSync(path: String, options: Json)
    fun createWriteStream(filePath: String): Writable
    fun removeSync(path: String)

}

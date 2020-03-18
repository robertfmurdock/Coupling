package com.zegreatrob.coupling.export.external.readline

import kotlin.js.json


fun inputReader() = createInterface(json("input" to js("process.stdin")))

fun ReadLine.onNewLine(handler: (String) -> Unit) = on("line", handler)

fun ReadLine.onEnd(handler: (String) -> Unit) = on("end", handler)
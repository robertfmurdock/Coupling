package com.zegreatrob.coupling.cdnLookup

import kotlin.js.Json

internal fun objectKeys(json: Json): List<String> = js("Object.keys")(json).unsafeCast<Array<String>>().toList()

internal fun packageNameForImport(importName: String): String {
    val segments = importName.split("/")
    return if (importName.startsWith("@")) {
        segments.take(2).joinToString("/")
    } else {
        segments.first()
    }
}

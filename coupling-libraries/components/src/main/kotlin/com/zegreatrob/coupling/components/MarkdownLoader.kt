package com.zegreatrob.coupling.components

fun loadMarkdownString(@Suppress("UNUSED_PARAMETER") name: String): String {
    return js("require('com/zegreatrob/coupling/client/'+ name +'.md')").default.toString()
}

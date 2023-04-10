package com.zegreatrob.coupling.client.components

fun loadMarkdownString(@Suppress("UNUSED_PARAMETER") name: String): String = if (js("global.IS_JSDOM") == true) {
    name
} else {
    js("require('com/zegreatrob/coupling/client/'+ name +'.md')").default.toString()
}

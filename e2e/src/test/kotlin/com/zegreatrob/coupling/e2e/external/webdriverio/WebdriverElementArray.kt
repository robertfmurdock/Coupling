package com.zegreatrob.coupling.e2e.external.webdriverio

class WebdriverElementArray(
    val selector: String = "",
    private val finder: suspend () -> List<WebdriverElement> = {
        WebdriverBrowser.all(selector)
            .map { WebdriverElement { it } }
    }
) : BrowserLoggingSyntax {
    private suspend fun all() = finder()

    fun get(index: Int) =
        WebdriverElement { all()[index].element() }

    suspend fun <T> map(transform: suspend (WebdriverElement) -> T) = log("map") {
        all().map { transform(it) }.toList()
    }

    suspend fun count() = log(::count) { all().count() }
    suspend fun first() = log(::first) { all().first() }

}

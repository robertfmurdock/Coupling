package com.zegreatrob.coupling.wdio

class WebdriverElementArray(
    val selector: String = "",
    private val finder: suspend () -> List<WebdriverElement> = selector.defaultArrayFinder()
) : BrowserLoggingSyntax {

    private suspend fun all() = finder()

    fun get(index: Int) = WebdriverElement { all()[index].element() }

    suspend fun <T> map(transform: suspend (WebdriverElement) -> T) = log("map") {
        all().map { transform(it) }.toList()
    }

    suspend fun count() = log(::count) { all().count() }
    suspend fun first() = log(::first) { all().first() }

}

private fun String.defaultArrayFinder(): suspend () -> List<WebdriverElement> = {
    WebdriverBrowser.all(this)
        .map { WebdriverElement { it } }
}

package com.zegreatrob.coupling.server.e2e.external.webdriverio

class WebdriverElementArray(
    val selector: String = "",
    private val finder: suspend () -> List<WebdriverElement> = {
        WebdriverBrowser.all(selector)
            .map { WebdriverElement { it } }
    }
) {
    private suspend fun all() = finder()

    suspend fun <T> map(transform: suspend (WebdriverElement) -> T) = all().map { transform(it) }.toList()

    suspend fun count() = all().count()
    suspend fun first() = all().first()
    fun get(index: Int) = WebdriverElement { all()[index].element() }
}

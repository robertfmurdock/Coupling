package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.webdriverio.BrowserSyntax

interface StyleSyntax : BrowserSyntax {
    val styles: SimpleStyle
    suspend fun getting(propertyName: String) = styles.element(propertyName)

    fun getting() = styles.getting()

    suspend fun element() = styles.element()
}

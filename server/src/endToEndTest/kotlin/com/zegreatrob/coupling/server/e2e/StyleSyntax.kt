package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.webdriverio.BrowserSyntax

interface StyleSyntax : BrowserSyntax {
    val styles: SimpleStyle
    suspend fun getting(propertyName: String) = styles.element(propertyName)
    suspend fun element() = styles.element()
}

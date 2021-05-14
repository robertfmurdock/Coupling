package com.zegreatrob.coupling.e2e.test.external.webdriverio

import com.zegreatrob.coupling.e2e.test.external.webdriverio.webdriverio.BrowserSyntax

interface StyleSyntax : BrowserSyntax {
    val styles: SimpleStyle
    suspend fun getting(propertyName: String) = styles.element(propertyName)

    fun getting() = styles.getting()

    fun element() = styles.element
}

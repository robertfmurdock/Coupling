package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax

interface StyleSyntax : BrowserSyntax {
    val styles: SimpleStyle

    fun element() = styles.element
}

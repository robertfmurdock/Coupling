package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.protractor.ProtractorSyntax

interface StyleSyntax : ProtractorSyntax {
    val styles: SimpleStyle
    fun getting() = styles.getting()
    val element get() = styles.element()
}

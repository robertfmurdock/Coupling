package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.protractor.ProtractorSyntax

object TribeCard : ProtractorSyntax {
    val tribeCardStyles = loadStyles("tribe/TribeCard")
    val element = tribeCardStyles.element()
    val header by tribeCardStyles.getting()
}
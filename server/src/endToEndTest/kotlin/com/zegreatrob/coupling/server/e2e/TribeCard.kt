package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.protractor.By
import com.zegreatrob.coupling.server.e2e.external.protractor.element

object TribeCard {
    val tribeCardStyles = loadStyles("tribe/TribeCard")
    val element = StatisticsPage.elementFor(tribeCardStyles)
    val header = element(By.className(tribeCardStyles["header"]))
}
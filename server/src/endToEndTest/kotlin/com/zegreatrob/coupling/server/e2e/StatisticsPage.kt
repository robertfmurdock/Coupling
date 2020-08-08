package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.webdriverio.By
import com.zegreatrob.coupling.server.e2e.external.webdriverio.WebdriverBrowser
import com.zegreatrob.coupling.server.e2e.external.webdriverio.waitToBePresent

object StatisticsPage : StyleSyntax {
    override val styles = loadStyles("stats/TribeStatistics")
    private val teamStatisticsStyles = loadStyles("stats/TeamStatistics")
    private val pairReportTableStyles = loadStyles("stats/PairReportTable")

    suspend fun getRotationNumber() = teamStatisticsStyles.element("rotationNumber")
    suspend fun getPairReports() = WebdriverBrowser.all(By.className(pairReportTableStyles["pairReport"]))

    suspend fun goTo(tribeId: TribeId) {
        setLocation("/${tribeId.value}/statistics")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToBePresent()
    }

}

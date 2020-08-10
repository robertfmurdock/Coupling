package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.e2e.external.webdriverio.WebdriverBrowser

object StatisticsPage : StyleSyntax {
    override val styles = loadStyles("stats/TribeStatistics")
    private val teamStatisticsStyles = loadStyles("stats/TeamStatistics")
    private val pairReportTableStyles = loadStyles("stats/PairReportTable")

    val rotationNumber by teamStatisticsStyles.getting()
    val pairReport by pairReportTableStyles.getAll()

    suspend fun goTo(tribeId: TribeId) {
        WebdriverBrowser.setLocation("/${tribeId.value}/statistics")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToExist()
    }
}

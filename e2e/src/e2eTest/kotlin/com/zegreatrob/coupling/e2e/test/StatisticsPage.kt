package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser

object StatisticsPage : StyleSyntax {
    override val styles = loadStyles("stats/TribeStatistics")
    private val teamStatisticsStyles = loadStyles("stats/TeamStatistics")
    private val pairReportTableStyles = loadStyles("stats/PairReportTable")

    val rotationNumber by teamStatisticsStyles.getting()
    val pairReport by pairReportTableStyles.getAll()

    suspend fun goTo(tribeId: PartyId) {
        WebdriverBrowser.setLocation("/${tribeId.value}/statistics")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToExist()
    }
}

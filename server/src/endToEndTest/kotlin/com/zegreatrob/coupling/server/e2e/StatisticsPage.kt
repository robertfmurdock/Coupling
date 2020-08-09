package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId

object StatisticsPage : StyleSyntax {
    override val styles = loadStyles("stats/TribeStatistics")
    private val teamStatisticsStyles = loadStyles("stats/TeamStatistics")
    private val pairReportTableStyles = loadStyles("stats/PairReportTable")

    val rotationNumber by teamStatisticsStyles.getting()
    val pairReport by pairReportTableStyles.getAll()

    suspend fun goTo(tribeId: TribeId) {
        setLocation("/${tribeId.value}/statistics")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToBePresent()
    }
}

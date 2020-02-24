package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.*

object StatisticsPage : ProtractorSyntax {

    private val tribeStatisticsStyles = loadStyles("stats/TribeStatistics")
    private val teamStatisticsStyles = loadStyles("stats/TeamStatistics")
    private val pairReportTableStyles = loadStyles("stats/PairReportTable")

    val tribeStatisticsElement = elementFor(tribeStatisticsStyles)
    val fullRotationNumber = element(By.className(teamStatisticsStyles["rotationNumber"]))
    val pairReports = all(By.className(pairReportTableStyles["pairReport"]))

    suspend fun goTo(tribeId: TribeId) {
        setLocation("/${tribeId.value}/statistics")
        waitForPage()
    }

    suspend fun waitForPage() {
        tribeStatisticsElement.waitToBePresent()
    }

}

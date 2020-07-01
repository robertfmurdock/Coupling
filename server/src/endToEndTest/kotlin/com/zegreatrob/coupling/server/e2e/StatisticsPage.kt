package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.By
import com.zegreatrob.coupling.server.e2e.external.protractor.ProtractorSyntax
import com.zegreatrob.coupling.server.e2e.external.protractor.all
import com.zegreatrob.coupling.server.e2e.external.protractor.waitToBePresent

object StatisticsPage : ProtractorSyntax {

    private val tribeStatisticsStyles = loadStyles("stats/TribeStatistics")
    private val teamStatisticsStyles = loadStyles("stats/TeamStatistics")
    private val pairReportTableStyles = loadStyles("stats/PairReportTable")

    private val tribeStatisticsElement = tribeStatisticsStyles.element()
    val rotationNumber by teamStatisticsStyles.getting()
    val pairReports = all(By.className(pairReportTableStyles["pairReport"]))

    suspend fun goTo(tribeId: TribeId) {
        setLocation("/${tribeId.value}/statistics")
        waitForPage()
    }

    suspend fun waitForPage() {
        tribeStatisticsElement.waitToBePresent()
    }

}

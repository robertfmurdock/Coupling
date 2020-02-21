package com.zegreatrob.coupling.server.e2e.external.protractor

import com.zegreatrob.coupling.server.e2e.SimpleStyle
import kotlinx.coroutines.await

interface ProtractorSyntax {

    suspend fun setLocation(location: String) {
        browser.get("${browser.baseUrl}$location").await()
    }

    suspend fun browserGoTo(url: String) = browser.get(url).await()

    fun elementFor(simpleStyle: SimpleStyle) = element(By.className(simpleStyle.className))

}
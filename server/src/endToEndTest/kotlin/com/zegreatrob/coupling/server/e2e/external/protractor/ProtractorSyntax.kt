package com.zegreatrob.coupling.server.e2e.external.protractor

import com.zegreatrob.coupling.server.e2e.SimpleStyle
import com.zegreatrob.coupling.server.e2e.get
import com.zegreatrob.minassert.assertIsEqualTo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise

interface ProtractorSyntax {

    suspend fun setLocation(location: String) {
        browser.get("${browser.baseUrl}$location").await()
    }

    suspend fun browserGoTo(url: String) = browser.get(url).await()

    fun elementFor(simpleStyle: SimpleStyle) = element(By.className(simpleStyle.className))

    fun SimpleStyle.elementWithClass(className: String) = element(By.className(this[className]))

    suspend fun waitToArriveAt(expectedUrl: String) {

        browser.wait({
            try {
                browser.getCurrentUrl().then { currentUrl ->
                    currentUrl.startsWith(expectedUrl)
                }
            } catch (bad: Throwable) {
                GlobalScope.promise { false }
            }
        }, 5000, "")

        val finalUrl = browser.getCurrentUrl().await()
        finalUrl.startsWith(expectedUrl).assertIsEqualTo(true)
    }

}
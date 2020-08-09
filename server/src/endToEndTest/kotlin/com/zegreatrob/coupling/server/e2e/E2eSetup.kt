package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.server.e2e.external.webdriverio.browser
import com.zegreatrob.coupling.testlogging.JasmineJsonLoggingReporter
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlinx.coroutines.await

val e2eSetup: TestTemplate<AuthorizedSdk> by lazy {
    JasmineJsonLoggingReporter.initialize()

    asyncTestTemplate(beforeAll = {
        CouplingLogin.sdkProvider.await()
            .also {
                browser.url("/").await()
                browser.executeAsync(
                    { _, done -> js("window.sessionStorage.setItem('animationDisabled', true)"); done() },
                    undefined
                ).await()
                DataLoadWrapper.getViewFrame().waitToBePresent()

                TestLogin.login(it.userEmail)
                browser.getLogs("browser").await()
            }
    }).extend(
        sharedTeardown = { checkLogs() }
    )
}

object DataLoadWrapper : BrowserSyntax, StyleSyntax {
    override val styles = loadStyles("routing/DataLoadWrapper")

    suspend fun getViewFrame() = styles.element("viewFrame")
}
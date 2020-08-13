package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.e2e.external.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.wdio.WebdriverBrowser
import com.zegreatrob.coupling.testlogging.JasmineJsonLoggingReporter
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncTestTemplate

val e2eSetup: TestTemplate<AuthorizedSdk> by lazy {
    JasmineJsonLoggingReporter.initialize()

    asyncTestTemplate(beforeAll = {
        CouplingLogin.sdkProvider.await()
            .also {
                WebdriverBrowser.setUrl("/")
                WebdriverBrowser.executeAsync(undefined) { _, done ->
                    js("window.sessionStorage.setItem('animationDisabled', true)")
                    done()
                }
                DataLoadWrapper.getViewFrame().waitToExist()

                TestLogin.login(it.userEmail)
                WebdriverBrowser.getLogs()
            }
    }).extend(
        sharedTeardown = { checkLogs() }
    )
}


object DataLoadWrapper : BrowserSyntax, StyleSyntax {
    override val styles = loadStyles("routing/DataLoadWrapper")

    suspend fun getViewFrame() = styles.element("viewFrame")
}
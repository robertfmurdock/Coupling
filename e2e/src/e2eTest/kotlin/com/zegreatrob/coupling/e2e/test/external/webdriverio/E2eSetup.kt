package com.zegreatrob.coupling.e2e.test.external.webdriverio

import com.zegreatrob.coupling.e2e.test.external.webdriverio.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.testlogging.JasmineJsonLoggingReporter
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlinx.coroutines.await
import kotlin.js.Promise

val e2eSetup: TestTemplate<AuthorizedSdk> by lazy {
    JasmineJsonLoggingReporter.initialize()

    asyncTestTemplate(beforeAll = {
        CouplingLogin.sdkProvider.await()
            .also { sdk ->
                sdk.getTribes().forEach { sdk.delete(it.data.id) }

                WebdriverBrowser.setUrl("")
                js("browser.executeAsync(function(ignore, done) {window.sessionStorage.setItem('animationDisabled', true); done()}, undefined)")
                    .unsafeCast<Promise<Unit>>()
                    .await()
                DataLoadWrapper.getViewFrame().waitToExist()

                TestLogin.login(sdk.userEmail)
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
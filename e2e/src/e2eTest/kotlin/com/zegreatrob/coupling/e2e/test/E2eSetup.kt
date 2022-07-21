package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.coupling.testlogging.JasmineJsonLoggingReporter
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlinx.coroutines.await
import kotlin.js.Promise

val e2eSetup: TestTemplate<Sdk> by lazy {
    JasmineJsonLoggingReporter.initialize()

    asyncTestTemplate(beforeAll = {
        CouplingLogin.sdkProvider.await().apply {
            getParties().map(Party::id).forEach { it.deleteIt() }

            WebdriverBrowser.setUrl("")
            js("browser.executeAsync(function(ignore, done) {window.sessionStorage.setItem('animationDisabled', true); done()}, undefined)")
                .unsafeCast<Promise<Unit>>()
                .await()
            DataLoadWrapper.getViewFrame().waitToExist()

            TestLogin.login()
            WebdriverBrowser.getLogs()
                .forwardLogs()
        }
    }).extend(
        sharedTeardown = { checkLogs() }
    )
}

object DataLoadWrapper : BrowserSyntax, StyleSyntax {
    override val styles = loadStyles("routing/DataLoadWrapper")

    suspend fun getViewFrame() = styles.element("viewFrame")
}

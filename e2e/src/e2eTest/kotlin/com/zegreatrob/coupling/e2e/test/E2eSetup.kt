package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.KtorCouplingSdk
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.testlogging.JasmineJsonLoggingReporter
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlinx.coroutines.await
import kotlin.js.Promise

val e2eSetup: TestTemplate<KtorCouplingSdk> by lazy {
    JasmineJsonLoggingReporter.initialize()

    asyncTestTemplate(beforeAll = {
        CouplingLogin.sdk.await().apply {
            perform(graphQuery { partyList() })
                ?.partyList
                ?.map(Record<PartyDetails>::data)
                ?.map(PartyDetails::id)
                ?.map { DeletePartyCommand(it) }
                ?.forEach { perform(it) }

            WebdriverBrowser.setUrl("")
            js("browser.executeAsync(function(ignore, done) {window.sessionStorage.setItem('animationDisabled', true); done()}, undefined)")
                .unsafeCast<Promise<Unit>>()
                .await()

            TestLogin.login()
            WebdriverBrowser.getLogs()
                .forwardLogs()
        }
    }).extend(
        sharedTeardown = { checkLogs() },
    )
}

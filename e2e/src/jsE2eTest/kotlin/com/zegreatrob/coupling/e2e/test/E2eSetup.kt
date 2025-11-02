package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.e2e.gql.PartyListQuery
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import com.zegreatrob.coupling.testlogging.JasmineJsonLoggingReporter
import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlinx.coroutines.await
import kotlin.js.Promise

val e2eSetup: TestTemplate<ActionCannon<CouplingSdkDispatcher>> by lazy {
    JasmineJsonLoggingReporter.initialize()

    asyncTestTemplate(beforeAll = {
        CouplingLogin.sdk.await().apply {
            fire(GqlQuery(PartyListQuery()))
                ?.partyList
                ?.mapNotNull { it.partyDetails.toDomain() }
                ?.map(PartyDetails::id)
                ?.map { DeletePartyCommand(it) }
                ?.forEach { fire(it) }

            WebdriverBrowser.setUrl("")
            js("browser.executeAsync(function(ignore, done) {window.sessionStorage.setItem('animationDisabled', true); window.sessionStorage.setItem('thirdPartyAvatarsDisabled', true); done()}, undefined)")
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

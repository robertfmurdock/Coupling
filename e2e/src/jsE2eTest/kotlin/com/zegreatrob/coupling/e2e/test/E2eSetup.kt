package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.e2e.gql.PartyListQuery
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.json

val e2eSetup: TestTemplate<ActionCannon<CouplingSdkDispatcher>> by lazy {
    asyncTestTemplate(beforeAll = {
        withTestIdentity(suite = "e2e.beforeAll", test = "bootstrap", testId = "e2e-beforeAll") {
            CouplingLogin.sdk.await().apply {
                fire(GqlQuery(PartyListQuery()))
                    ?.partyList
                    ?.map { it.partyDetails.toDomain() }
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
        }
    }).extend { context, test ->
        withTestIdentity(
            suite = "e2e.synthetic",
            test = "spec-${nextSyntheticTestIndex()}",
            testId = null,
        ) {
            val startMillis = nowMillis()
            appendTestLifecycleToTestLog(type = "TestStart")
            E2eJsonlTestMintsReporter.emitSetupStart()
            E2eJsonlTestMintsReporter.emitTestStart()
            var failed = false
            try {
                test(context)
            } catch (t: Throwable) {
                failed = true
                throw t
            } finally {
                try {
                    checkLogs()
                } catch (t: Throwable) {
                    failed = true
                    throw t
                } finally {
                    E2eJsonlTestMintsReporter.emitTestFinish()
                    appendTestLifecycleToTestLog(
                        type = "TestEnd",
                        status = if (failed) "FAILED" else "PASSED",
                        durationMs = nowMillis() - startMillis,
                    )
                }
            }
        }
    }
}

private fun nowMillis(): Double = (js("Date.now()") as Number).toDouble()

private suspend fun <T> withTestIdentity(
    suite: String,
    test: String,
    testId: String?,
    block: suspend () -> T,
): T {
    val globalThis = js("globalThis")
    val previous = globalThis.__couplingCurrentTest
    val process = js("typeof process !== 'undefined' ? process : null")
    val previousSuite = process?.env?.COUPLING_CURRENT_TEST_SUITE
    val previousTest = process?.env?.COUPLING_CURRENT_TEST_NAME
    val previousTestId = process?.env?.COUPLING_CURRENT_TEST_ID
    globalThis.__couplingCurrentTest = json(
        "suite" to suite,
        "test" to test,
        "testId" to testId,
    )
    if (process != null) {
        process.env.COUPLING_CURRENT_TEST_SUITE = suite
        process.env.COUPLING_CURRENT_TEST_NAME = test
        process.env.COUPLING_CURRENT_TEST_ID = testId ?: ""
    }
    return try {
        block()
    } finally {
        globalThis.__couplingCurrentTest = previous
        if (process != null) {
            if (previousSuite != null) process.env.COUPLING_CURRENT_TEST_SUITE = previousSuite else js("delete process.env.COUPLING_CURRENT_TEST_SUITE")
            if (previousTest != null) process.env.COUPLING_CURRENT_TEST_NAME = previousTest else js("delete process.env.COUPLING_CURRENT_TEST_NAME")
            if (previousTestId != null) process.env.COUPLING_CURRENT_TEST_ID = previousTestId else js("delete process.env.COUPLING_CURRENT_TEST_ID")
        }
    }
}

private fun nextSyntheticTestIndex(): Int {
    val globalThis = js("globalThis")
    val current = (globalThis.__couplingSyntheticTestCounter as? Number)?.toInt() ?: 0
    val next = current + 1
    globalThis.__couplingSyntheticTestCounter = next
    return next
}

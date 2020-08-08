package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.testlogging.JasmineJsonLoggingReporter
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncTestTemplate

val e2eSetup: TestTemplate<AuthorizedSdk> by lazy {
    JasmineJsonLoggingReporter.initialize()

    asyncTestTemplate(beforeAll = {
        CouplingLogin.sdkProvider.await()
            .also {
                TestLogin.login(it.userEmail)
            }
    }).extend(
        sharedTeardown = { checkLogs() }
    )
}

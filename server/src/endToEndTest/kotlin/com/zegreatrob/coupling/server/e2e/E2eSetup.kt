package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncTestTemplate

val e2eSetup: TestTemplate<AuthorizedSdk> = asyncTestTemplate(beforeAll = {
    CouplingLogin.sdkProvider.await()
        .also {
            TestLogin.login(it.userEmail)
        }
}).extend(
    sharedSetup = { },
    sharedTeardown = { checkLogs() }
)

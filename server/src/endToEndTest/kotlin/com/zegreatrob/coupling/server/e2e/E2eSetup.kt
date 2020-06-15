package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.testmints.async.asyncTestTemplate

val e2eSetup = asyncTestTemplate(
    sharedSetup = { CouplingLogin.loginProvider.await() },
    sharedTeardown = { checkLogs() }
)
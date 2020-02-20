package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.server.e2e.external.protractor.ProtractorSyntax

object TestLogin : ProtractorSyntax {
    suspend fun login(userEmail: String) {
        browserGoTo("${Config.publicUrl}/test-login?username=${userEmail}&password=pw")
        TribeListPage.wait()
    }
}
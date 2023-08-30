package com.zegreatrob.coupling.cli

import com.zegreatrob.coupling.auth0.management.couplingCliClientId

data class Auth0Environment(val clientId: String, val audience: String) {
    companion object {
        val map = mapOf(
            "production" to Auth0Environment(
                couplingCliClientId,
                "https://coupling.zegreatrob.com/api",
            ),
            "prerelease" to Auth0Environment(
                couplingCliClientId,
                "https://prerelease.coupling.zegreatrob.com/api",
            ),
            "sandbox" to Auth0Environment(
                couplingCliClientId,
                "https://sandbox.coupling.zegreatrob.com/api",
            ),
            "local" to Auth0Environment(
                clientId = couplingCliClientId,
                audience = "https://localhost/api",
            ),
        )
    }
}

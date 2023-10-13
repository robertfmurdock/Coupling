package com.zegreatrob.coupling.cli

import com.zegreatrob.coupling.auth0.management.COUPLING_CLI_CLIENT_ID

data class Auth0Environment(val clientId: String, val audience: String) {
    companion object {
        val map = mapOf(
            "production" to Auth0Environment(
                COUPLING_CLI_CLIENT_ID,
                "https://coupling.zegreatrob.com/api",
            ),
            "prerelease" to Auth0Environment(
                COUPLING_CLI_CLIENT_ID,
                "https://prerelease.coupling.zegreatrob.com/api",
            ),
            "sandbox" to Auth0Environment(
                COUPLING_CLI_CLIENT_ID,
                "https://sandbox.coupling.zegreatrob.com/api",
            ),
            "local" to Auth0Environment(
                clientId = COUPLING_CLI_CLIENT_ID,
                audience = "https://localhost/api",
            ),
        )
    }
}

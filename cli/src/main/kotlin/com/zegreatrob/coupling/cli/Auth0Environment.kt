package com.zegreatrob.coupling.cli

import com.zegreatrob.coupling.cli.auth0.couplingCliClientId

data class Auth0Environment(val clientId: String, val audience: String) {
    companion object {
        val map = mapOf(
            "production" to Auth0Environment(
                couplingCliClientId,
                "https://coupling.zegreatrob.com/api",
            ),
        )
    }
}

package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.command.test
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class CouplingCliTest {

    @Test
    fun versionIsAvailable() = asyncSetup(object : ScopeMint() {
    }) exercise {
        cli().test("--version")
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.output)
        result.output.trim()
            .assertIsEqualTo("coupling-cli version ${getEnv("COUPLING_VERSION")}")
    }
}

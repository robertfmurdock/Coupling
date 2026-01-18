package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.command.test
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.serialization.json.Json.Default.decodeFromString
import kotlin.test.Test

class ConfigCommandCLITest {

    @Test
    fun givenPartyIsConfiguredWillReturnPartyDetailsFromQuery() = asyncSetup(object : ScopeMint() {
        val workingDir = createTempDirectory()
        val configFile = "$workingDir/.coupling"
        val partyId = stubPartyId()
    }) {
//        Json.encodeToString(
//            CouplingCliConfig(partyId = partyId),
//        )
//            .writeToFile(configFile)
    } exercise {
        cli()
            .test("config --party-id=${partyId.value}", envvars = mapOf("PWD" to workingDir))
    } verify { result ->
        result.statusCode.assertIsEqualTo(0, result.output)
        result.output.trim()
            .assertIsEqualTo("Updated file: $configFile")
        readFromFile(configFile)
            ?.let { decodeFromString(CouplingCliConfig.serializer(), it) }
            .assertIsEqualTo(CouplingCliConfig(partyId = partyId), "Config file should have been updated")
    }
}

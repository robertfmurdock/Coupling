package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyConfigurationBackfill
import com.zegreatrob.coupling.repository.party.PartyConfigurationBackfillResult
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class BackfillPartyConfigurationCommandTest {
    @Test
    fun dispatchesTheSelectedPartyAndReturnsDurableProgress() = asyncSetup(object {
        val command = BackfillPartyConfigurationCommand("sandbox", stubPartyId(), "stable-run")
        val expected = PartyConfigurationBackfillResult(3, "20240103000000.000", true)
        val calls = mutableListOf<BackfillCall>()
        val dispatcher = object : BackfillPartyConfigurationCommand.Dispatcher {
            override val partyConfigurationBackfill = PartyConfigurationBackfill { stage, partyId, runId ->
                calls.add(BackfillCall(stage, partyId, runId))
                expected
            }
        }
    }) { } exercise {
        dispatcher.perform(command)
    } verify { result ->
        result.assertIsEqualTo(expected)
        calls.assertIsEqualTo(listOf(BackfillCall("sandbox", command.partyId, "stable-run")))
    }
}

private data class BackfillCall(val stage: String, val partyId: PartyId, val runId: String)

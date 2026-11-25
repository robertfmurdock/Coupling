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
    fun dispatchesTheSelectedPartyAndReturnsTheConflictUnchanged() = asyncSetup(object {
        val command = BackfillPartyConfigurationCommand(stubPartyId(), "stable-run")
        val expected = PartyConfigurationBackfillResult(3, "20240103000000.000", complete = false, conflictingSourceKey = "20240102000000.000")
        val calls = mutableListOf<BackfillCall>()
        val dispatcher = object : BackfillPartyConfigurationCommand.Dispatcher {
            override val partyConfigurationBackfill = PartyConfigurationBackfill { partyId, runId ->
                calls.add(BackfillCall(partyId, runId))
                expected
            }
        }
    }) { } exercise {
        dispatcher.perform(command)
    } verify { result ->
        result.assertIsEqualTo(expected)
        calls.assertIsEqualTo(listOf(BackfillCall(command.partyId, command.runId)))
    }
}

private data class BackfillCall(val partyId: PartyId, val runId: String)

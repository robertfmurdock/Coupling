package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.Paths.newPlayerConfigPath
import com.zegreatrob.coupling.client.components.Paths.playerConfigPath
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotools.types.text.toNotBlankString
import kotlin.test.Test

class PathsTest {

    @Test
    fun playerConfigPath() = asyncSetup(object {
        val partyIdValue = uuidString()
        val partyId = PartyId(partyIdValue)
        val playerIdValue = "playerIdValue"
        val player = stubPlayer().copy(id = PlayerId(playerIdValue.toNotBlankString().getOrThrow()))
    }) exercise {
        partyId.with(player).playerConfigPath()
    } verify { result ->
        result.assertIsEqualTo("/$partyIdValue/player/$playerIdValue/")
    }

    @Test
    fun newPlayerConfigPath() = asyncSetup(object {
        val partyIdValue = uuidString()
        val partyId = PartyId(partyIdValue)
    }) exercise {
        partyId.newPlayerConfigPath()
    } verify { result ->
        result.assertIsEqualTo("/$partyIdValue/player/new/")
    }
}

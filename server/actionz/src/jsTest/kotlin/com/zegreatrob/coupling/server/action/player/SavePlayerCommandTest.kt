package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.player.PlayerSave
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class SavePlayerCommandTest {

    @Test
    fun willSaveToRepository() = asyncSetup(object : ServerSavePlayerCommandDispatcher {
        override val currentPartyId = PartyId("woo")
        val player = stubPlayer().copy(
            badge = Badge.Default,
            name = "Tim",
            email = "tim@tim.meat",
            callSignAdjective = "Spicy",
            callSignNoun = "Meatball",
            imageURL = "italian.jpg",
            avatarType = null,
        )
        override val playerRepository = PlayerSaverSpy().apply { whenever(currentPartyId.with(player), Unit) }
    }) exercise {
        perform(SavePlayerCommand(currentPartyId, player))
    } verify { result ->
        result.assertIsEqualTo(VoidResult.Accepted)
        playerRepository.spyReceivedValues
            .assertIsEqualTo(listOf(currentPartyId.with(player)))
    }

    class PlayerSaverSpy :
        PlayerSave,
        Spy<PartyElement<Player>, Unit> by SpyData() {
        override suspend fun save(partyPlayer: PartyElement<Player>) = spyFunction(partyPlayer)
    }
}

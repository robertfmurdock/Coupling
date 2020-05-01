package com.zegreatrob.coupling.server.action.player

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.player.PlayerSave
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class SavePlayerCommandTest {

    @Test
    fun willSaveToRepository() = asyncSetup(object : SavePlayerCommandDispatcher {
        val tribe = TribeId("woo")
        val player = Player(
            id = "1",
            badge = 1,
            name = "Tim",
            callSignAdjective = "Spicy",
            callSignNoun = "Meatball",
            email = "tim@tim.meat",
            imageURL = "italian.jpg"
        )
        override val traceId = uuid4()
        override val playerRepository = PlayerSaverSpy().apply { whenever(tribe.with(player), Unit) }
    }) exercise {
        SavePlayerCommand(tribe.with(player))
            .perform()
    } verify { result ->
        result.assertIsEqualTo(player)
    }

    class PlayerSaverSpy : PlayerSave, Spy<TribeIdPlayer, Unit> by SpyData() {
        override suspend fun save(tribeIdPlayer: TribeIdPlayer) = spyFunction(tribeIdPlayer)
    }
}
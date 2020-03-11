package com.zegreatrob.coupling.mongo.player

import Spy
import SpyData
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.player.PlayerSave
import com.zegreatrob.coupling.server.action.player.SavePlayerCommand
import com.zegreatrob.coupling.server.action.player.SavePlayerCommandDispatcher
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.test.Test

class SavePlayerCommandTest {

    @Test
    fun willSaveToRepository() = testAsync {
        setupAsync(object : SavePlayerCommandDispatcher {
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
            override val traceId = null
            override val playerRepository = PlayerSaverSpy().apply { whenever(tribe.with(player), Unit) }
        }) exerciseAsync {
            SavePlayerCommand(tribe.with(player))
                .perform()
        } verifyAsync { result ->
            result.assertIsEqualTo(player)
        }
    }

    class PlayerSaverSpy : PlayerSave, Spy<TribeIdPlayer, Unit> by SpyData() {
        override suspend fun save(tribeIdPlayer: TribeIdPlayer) = spyFunction(tribeIdPlayer)
    }
}
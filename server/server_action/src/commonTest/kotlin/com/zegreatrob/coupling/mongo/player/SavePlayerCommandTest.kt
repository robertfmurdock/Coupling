package com.zegreatrob.coupling.mongo.player

import Spy
import SpyData
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.player.PlayerSaver
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.with
import com.zegreatrob.coupling.model.tribe.TribeId
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
            override val playerRepository = PlayerSaverSpy().apply { whenever(player with tribe, Unit) }
        }) exerciseAsync {
            SavePlayerCommand(player with tribe)
                .perform()
        } verifyAsync { result ->
            result.assertIsEqualTo(player)
        }
    }

    class PlayerSaverSpy : PlayerSaver, Spy<TribeIdPlayer, Unit> by SpyData() {
        override suspend fun save(tribeIdPlayer: TribeIdPlayer) = spyFunction(tribeIdPlayer)
    }
}
package com.zegreatrob.coupling.server.entity.player

import Spy
import SpyData
import assertIsEqualTo
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.player.TribeIdPlayer
import com.zegreatrob.coupling.common.entity.player.with
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import exerciseAsync
import setupAsync
import testAsync
import verifyAsync
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
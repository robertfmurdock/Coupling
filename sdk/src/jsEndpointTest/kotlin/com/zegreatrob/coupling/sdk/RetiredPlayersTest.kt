package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.test.Test

@Suppress("unused")
class RetiredPlayersTest {
    class GET {
        @Test
        fun givenRetiredPlayerReturnsAllRetiredPlayers() = testAsync {
            val sdk = authorizedSdk()
            setupAsync(object {
                val tribe = Tribe(id = TribeId("et-${uuid4()}"))
                val player = Player(
                    id = monk.id().toString(),
                    name = "Retiree"
                )
            }) {
                sdk.save(tribe)
                sdk.save(TribeIdPlayer(tribe.id, player))
                sdk.deletePlayer(tribe.id, player.id!!)
            } exerciseAsync {
                sdk.getDeleted(tribe.id)
            } verifyAsync { result ->
                result.assertIsEqualTo(listOf(player))
            }
        }
    }
}
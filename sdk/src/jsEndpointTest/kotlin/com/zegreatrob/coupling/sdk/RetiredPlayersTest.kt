package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.external.axios.Axios
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.test.Test

@Suppress("unused")
class RetiredPlayersTest {

    class GET {
        @Test
        fun givenRetiredPlayerReturnsAllRetiredPlayers() = testAsync {
            val hostAxios = authorizedAxios()
            setupAsync(object : SdkPlayerSaver, SdkTribeSave, SdkPlayerGetDeleted, SdkPlayerDeleter {
                override val axios: Axios get() = hostAxios
                val tribe = KtTribe(id = TribeId("et-${uuid4()}"))
                val player = Player(
                    id = monk.id().toString(),
                    name = "Retiree"
                )
            }) {
                save(tribe)
                save(TribeIdPlayer(tribe.id, player))
                deletePlayer(tribe.id, player.id!!)
            } exerciseAsync {
                getDeletedAsync(tribe.id).await()
            } verifyAsync { result ->
                result.assertIsEqualTo(listOf(player))
            }
        }
    }

}
package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.repository.validation.PlayerRepositoryValidator
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import stubTribe
import kotlin.js.Json
import kotlin.js.json
import kotlin.test.Test

class SdkPlayerRepositoryTest :
    PlayerRepositoryValidator {

    override suspend fun withRepository(handler: suspend (PlayerRepository, TribeId) -> Unit) {
        val sdk = authorizedSdk(username = "eT-user-${uuid4()}")
        val tribe = stubTribe()
        sdk.save(tribe)
        handler(sdk, tribe.id)
    }

    companion object {
        inline fun catchAxiosError(function: () -> Any?) = try {
            function()
            json()
        } catch (error: dynamic) {
            error.response.unsafeCast<Json>()
        }
    }

    class GivenUsersWithoutAccess {
        companion object {
            val tribeId = TribeId("somebodyElsesTribe")
        }

        @Test
        fun getIsNotAllowed() = testAsync {
            val sdk = authorizedSdk()
            setupAsync(object {}) exerciseAsync {
                sdk.getPlayers(tribeId)
            } verifyAsync { result ->
                result.assertIsEqualTo(emptyList())
            }
        }

        @Test
        fun postIsNotAllowed() = testAsync {
            val sdk = authorizedSdk()
            setupAsync(object {
                val player = Player(
                    id = "${uuid4()}",
                    name = "Awesome-O",
                    callSignAdjective = "Awesome",
                    callSignNoun = "Sauce"
                )
            }) exerciseAsync {
                catchAxiosError {
                    sdk.save(TribeIdPlayer(tribeId, player))
                }
            } verifyAsync { result ->
                result["status"].assertIsEqualTo(404)
            }
        }

        @Test
        fun deleteIsNotAllowed() = testAsync {
            val sdk = authorizedSdk()
            setupAsync(object {
            }) exerciseAsync {
                sdk.deletePlayer(tribeId, "player id")
            } verifyAsync { result ->
                result.assertIsEqualTo(false)
            }
        }
    }

}
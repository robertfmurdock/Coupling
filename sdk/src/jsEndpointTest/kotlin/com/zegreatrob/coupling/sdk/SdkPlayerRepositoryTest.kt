package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PlayerRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.js.Json
import kotlin.js.json
import kotlin.test.Test

class SdkPlayerRepositoryTest : PlayerRepositoryValidator<SdkPlayerRepository> {

    override suspend fun withRepository(
        clock: MagicClock,
        handler: suspend (SdkPlayerRepository, TribeId, User) -> Unit
    ) {
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        val tribe = stubTribe()
        sdk.save(tribe)
        val user = stubUser().copy(email = "$username._temp")
        handler(sdk, tribe.id, user)
    }

    companion object {
        inline fun catchAxiosError(function: () -> Any?) = try {
            function()
            json()
        } catch (error: dynamic) {
            error.response.unsafeCast<Json>()
        }
    }

    override fun deletedPlayersIncludeModificationDateAndUsername() = testRepository { repository, tribeId, _, _ ->
        setupAsync(object {
            val player = stubPlayer()
        }) exerciseAsync {
            repository.save(tribeId.with(player))
            repository.deletePlayer(tribeId, player.id!!)
            repository.getDeleted(tribeId)
        } verifyAsync { result ->
            result.size.assertIsEqualTo(1)
            result.first().apply {
                isDeleted.assertIsEqualTo(true)
                timestamp.assertIsCloseToNow()
                modifyingUserId.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
            }
        }
    }

    override fun savedPlayersIncludeModificationDateAndUsername() = testRepository { repository, tribeId, _, _ ->
        setupAsync(object {
            val player = stubPlayer()
        }) exerciseAsync {
            repository.save(tribeId.with(player))
            repository.getPlayers(tribeId)
        } verifyAsync { result ->
            result.size.assertIsEqualTo(1)
            result.first().apply {
                timestamp.assertIsCloseToNow()
                modifyingUserId.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
            }
        }
    }

    class GivenUsersWithoutAccess {

        @Test
        fun getIsNotAllowed() = testAsync {
            val sdk = authorizedSdk()
            val otherSdk = authorizedSdk("alt-user-${uuid4()}")
            setupAsync(object {
                val tribe = stubTribe()
            }) {
                otherSdk.save(tribe)
                otherSdk.save(tribe.id.with(stubPlayer()))
            } exerciseAsync {
                sdk.getPlayers(tribe.id)
            } verifyAsync { result ->
                result.assertIsEqualTo(emptyList())
            }
        }

        @Test
        fun postIsNotAllowed() = testAsync {
            val sdk = authorizedSdk()
            setupAsync(object {
                val tribe = stubTribe()
                val player = Player(
                    id = "${uuid4()}",
                    name = "Awesome-O",
                    callSignAdjective = "Awesome",
                    callSignNoun = "Sauce"
                )

            }) exerciseAsync {
                catchAxiosError {
                    sdk.save(tribe.id.with(player))
                }
            } verifyAsync { result ->
                result["status"].assertIsEqualTo(404)
            }
        }

        @Test
        fun deleteIsNotAllowed() = testAsync {
            val sdk = authorizedSdk()
            setupAsync(object {
                val tribe = stubTribe()
            }) exerciseAsync {
                sdk.deletePlayer(tribe.id, "player id")
            } verifyAsync { result ->
                result.assertIsEqualTo(false)
            }
        }
    }

}
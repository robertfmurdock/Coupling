package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.testmints.async.testAsync
import com.zegreatrob.testmints.async.waitForTest
import kotlin.js.Json
import kotlin.js.json
import kotlin.test.Test

class SdkPlayerRepositoryTest : PlayerRepositoryValidator<Sdk> {

    override val repositorySetup = asyncTestTemplate<TribeSharedContext<Sdk>>(sharedSetup = {

        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        val tribe = stubTribe()
        sdk.save(tribe)
        val user = stubUser().copy(email = "$username._temp")

        object : TribeSharedContext<Sdk> {
            override val tribeId = tribe.id
            override val repository = sdk
            override val clock = MagicClock()
            override val user = user
        }
    })

    companion object {
        inline fun catchAxiosError(function: () -> Any?) = try {
            function()
            json()
        } catch (error: dynamic) {
            error.response.unsafeCast<Json>()
        }
    }

    override fun deletedPlayersIncludeModificationDateAndUsername() =
        repositorySetup(object : PlayerContextMint<Sdk>() {
            val player = stubPlayer()
        }.bind()) {
        } exercise {
            repository.save(tribeId.with(player))
            repository.deletePlayer(tribeId, player.id!!)
            repository.getDeleted(tribeId)
        } verify { result ->
            result.size.assertIsEqualTo(1)
            result.first().apply {
                isDeleted.assertIsEqualTo(true)
                timestamp.assertIsCloseToNow()
                modifyingUserId.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
            }
        }

    override fun savedPlayersIncludeModificationDateAndUsername() = repositorySetup(object : PlayerContextMint<Sdk>() {
        val player = stubPlayer()
    }.bind()) {
    } exercise {
        repository.save(tribeId.with(player))
        repository.getPlayers(tribeId)
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            timestamp.assertIsCloseToNow()
            modifyingUserId.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
        }
    }

    class GivenUsersWithoutAccess {

        @Test
        fun getIsNotAllowed() = testAsync {
            val sdk = authorizedSdk()
            val otherSdk = authorizedSdk("alt-user-${uuid4()}")
            waitForTest {
                asyncSetup(object {
                    val tribe = stubTribe()
                }) {
                    otherSdk.save(tribe)
                    otherSdk.save(tribe.id.with(stubPlayer()))
                } exercise {
                    sdk.getPlayers(tribe.id)
                } verify { result ->
                    result.assertIsEqualTo(emptyList())
                }
            }
        }

        @Test
        fun postIsNotAllowed() = testAsync {
            val sdk = authorizedSdk()
            waitForTest {
                asyncSetup(object {
                    val tribe = stubTribe()
                    val player = Player(
                        id = "${uuid4()}",
                        name = "Awesome-O",
                        callSignAdjective = "Awesome",
                        callSignNoun = "Sauce"
                    )
                }) exercise {
                    catchAxiosError {
                        sdk.save(tribe.id.with(player))
                    }
                } verify { result ->
                    result["status"].assertIsEqualTo(404)
                }
            }
        }

        @Test
        fun deleteIsNotAllowed() = testAsync {
            val sdk = authorizedSdk()
            waitForTest {
                asyncSetup(object {
                    val tribe = stubTribe()
                }) exercise {
                    sdk.deletePlayer(tribe.id, "player id")
                } verify { result ->
                    result.assertIsEqualTo(false)
                }
            }
        }
    }

}
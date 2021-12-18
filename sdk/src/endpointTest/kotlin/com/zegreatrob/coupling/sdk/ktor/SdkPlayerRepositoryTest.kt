package com.zegreatrob.coupling.sdk.ktor

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.*
import kotlin.test.Test

class SdkPlayerRepositoryTest : PlayerRepositoryValidator<Sdk> {

    override val repositorySetup = asyncTestTemplate<TribeContext<Sdk>>(sharedSetup = {

        val username = "eT-user-${uuid4()}"
        val sdk = authorizedKtorSdk(username = username)
        val tribe = stubTribe()
        sdk.save(tribe)
        val user = stubUser().copy(email = "$username._temp")

        object : TribeContext<Sdk> {
            override val tribeId = tribe.id
            override val repository = sdk
            override val clock = MagicClock()
            override val user = user
        }
    })

    override fun whenPlayerIdIsUsedInTwoDifferentTribesTheyRemainDistinct() =
        repositorySetup(object : TribeContextMint<Sdk>() {
            val player1 = stubPlayer()
            val tribeId2 = stubTribeId()
            val player2 = player1.copy(id = player1.id)
        }.bind()) {
            repository.save(stubTribe().copy(id = tribeId2))
            repository.save(tribeId.with(player1))
            repository.save(tribeId2.with(player2))
        } exercise {
            repository.getPlayers(tribeId)
        } verify { result ->
            result.map { it.data.player }
                .assertIsEqualTo(listOf(player1))
        }

    override fun deletedPlayersIncludeModificationDateAndUsername() = repositorySetup(object : TribeContextMint<Sdk>() {
        val player = stubPlayer()
    }.bind()) {
    } exercise {
        repository.save(tribeId.with(player))
        repository.deletePlayer(tribeId, player.id)
        repository.getDeleted(tribeId)
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            isDeleted.assertIsEqualTo(true)
            timestamp.assertIsCloseToNow()
            modifyingUserId.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
        }
    }

    override fun savedPlayersIncludeModificationDateAndUsername() = repositorySetup(object : TribeContextMint<Sdk>() {
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
            val sdk = authorizedKtorSdk()
            val otherSdk = authorizedKtorSdk("alt-user-${uuid4()}")
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
            val sdk = authorizedKtorSdk()
            val otherSdk = authorizedKtorSdk("alt-user-${uuid4()}")
            waitForTest {
                asyncSetup(object {
                    val tribe = stubTribe()
                    val player = Player(
                        id = "${uuid4()}",
                        name = "Awesome-O",
                        callSignAdjective = "Awesome",
                        callSignNoun = "Sauce"
                    )
                }) {
                    otherSdk.save(tribe)
                } exercise {
                    sdk.save(tribe.id.with(player))
                    otherSdk.getPlayers(tribe.id)
                } verify { result ->
                    result.assertIsEqualTo(emptyList())
                }
            }
        }

        @Test
        fun deleteIsNotAllowed() = testAsync {
            val sdk = authorizedKtorSdk()
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
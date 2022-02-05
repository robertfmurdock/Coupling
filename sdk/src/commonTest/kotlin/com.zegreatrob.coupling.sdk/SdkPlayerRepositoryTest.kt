package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.testmints.async.invoke
import com.zegreatrob.testmints.async.waitForTest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SdkPlayerRepositoryTest : PlayerRepositoryValidator<Sdk> {

    override val repositorySetup = asyncTestTemplate<TribeContext<Sdk>>(sharedSetup = {
        val sdk = authorizedKtorSdk()
        val tribe = stubTribe()
        sdk.tribeRepository.save(tribe)
        val user = stubUser().copy(email = primaryAuthorizedUsername)

        object : TribeContext<Sdk> {
            override val tribeId = tribe.id
            override val repository = sdk
            override val clock = MagicClock()
            override val user = user
        }
    }, sharedTeardown = {
        it.repository.tribeRepository.delete(it.tribeId)
    })

    override fun whenPlayerIdIsUsedInTwoDifferentTribesTheyRemainDistinct() =
        repositorySetup(object : TribeContextMint<Sdk>() {
            val player1 = stubPlayer()
            val tribeId2 = stubTribeId()
            val player2 = player1.copy(id = player1.id)
        }.bind()) {
            repository.tribeRepository.save(stubTribe().copy(id = tribeId2))
            repository.save(tribeId.with(player1))
            repository.save(tribeId2.with(player2))
        } exercise {
            repository.getPlayers(tribeId)
        } verifyAnd { result ->
            result.map { it.data.player }
                .assertIsEqualTo(listOf(player1))
        } teardown {
            repository.tribeRepository.delete(tribeId2)
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
        fun getIsNotAllowed() = runTest {
            val sdk = authorizedKtorSdk()
            val otherSdk = altAuthorizedSdkDeferred.await()
            waitForTest {
                asyncSetup(object {
                    val tribe = stubTribe()
                }) {
                    otherSdk.tribeRepository.save(tribe)
                    otherSdk.save(tribe.id.with(stubPlayer()))
                } exercise {
                    sdk.getPlayers(tribe.id)
                } verifyAnd { result ->
                    result.assertIsEqualTo(emptyList())
                } teardown {
                    otherSdk.tribeRepository.delete(tribe.id)
                }
            }
        }

        @Test
        fun postIsNotAllowed() = runTest {
            val sdk = authorizedKtorSdk()
            val otherSdk = altAuthorizedSdkDeferred.await()
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
                    otherSdk.tribeRepository.save(tribe)
                } exercise {
                    sdk.save(tribe.id.with(player))
                    otherSdk.getPlayers(tribe.id)
                } verifyAnd { result ->
                    result.assertIsEqualTo(emptyList())
                } teardown {
                    otherSdk.tribeRepository.delete(tribe.id)
                }
            }
        }

        @Test
        fun deleteIsNotAllowed() = runTest {
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

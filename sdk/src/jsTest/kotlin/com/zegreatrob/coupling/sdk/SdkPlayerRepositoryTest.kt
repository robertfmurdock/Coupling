package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyContextMint
import com.zegreatrob.coupling.repository.validation.PlayerRepositoryValidator
import com.zegreatrob.coupling.repository.validation.bind
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.testmints.async.waitForTest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SdkPlayerRepositoryTest : PlayerRepositoryValidator<SdkPlayerRepository> {

    override val repositorySetup = asyncTestTemplate<SdkPartyContext<SdkPlayerRepository>>(sharedSetup = {
        val sdk = authorizedSdk()
        val party = stubParty()
        sdk.partyRepository.save(party)

        SdkPartyContext(sdk, sdk.playerRepository, party.id, MagicClock())
    }, sharedTeardown = {
        it.sdk.partyRepository.deleteIt(it.partyId)
    })

    override fun whenPlayerIdIsUsedInTwoDifferentPartiesTheyRemainDistinct() =
        repositorySetup.with({ parent: SdkPartyContext<SdkPlayerRepository> ->
            object {
                val sdk = parent.sdk
                val repository = parent.repository
                val partyId = parent.partyId

                val player1 = stubPlayer()
                val partyId2 = stubPartyId()
                val player2 = player1.copy(id = player1.id)
            }
        }) {
            sdk.partyRepository.save(stubParty().copy(id = partyId2))
            repository.save(partyId.with(player1))
            repository.save(partyId2.with(player2))
        } exercise {
            repository.getPlayers(partyId)
        } verifyAnd { result ->
            result.map { it.data.player }
                .assertIsEqualTo(listOf(player1))
        } teardown {
            sdk.partyRepository.deleteIt(partyId2)
        }

    override fun deletedPlayersIncludeModificationDateAndUsername() =
        repositorySetup.with(
            object : PartyContextMint<SdkPlayerRepository>() {
                val player = stubPlayer()
            }.bind(),
        ) {
        } exercise {
            repository.save(partyId.with(player))
            repository.deletePlayer(partyId, player.id)
            repository.getDeleted(partyId)
        } verify { result ->
            result.size.assertIsEqualTo(1)
            result.first().apply {
                isDeleted.assertIsEqualTo(true)
                timestamp.assertIsCloseToNow()
                modifyingUserId.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
            }
        }

    override fun savedPlayersIncludeModificationDateAndUsername() =
        repositorySetup.with(
            object : PartyContextMint<SdkPlayerRepository>() {
                val player = stubPlayer()
            }.bind(),
        ) {
        } exercise {
            repository.save(partyId.with(player))
            repository.getPlayers(partyId)
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
            val sdk = authorizedSdk()
            val otherSdk = altAuthorizedSdkDeferred.await()
            waitForTest {
                asyncSetup(object {
                    val party = stubParty()
                }) {
                    otherSdk.partyRepository.save(party)
                    otherSdk.playerRepository.save(party.id.with(stubPlayer()))
                } exercise {
                    sdk.playerRepository.getPlayers(party.id)
                } verifyAnd { result ->
                    result.assertIsEqualTo(emptyList())
                } teardown {
                    otherSdk.partyRepository.deleteIt(party.id)
                }
            }
        }

        @Test
        fun postIsNotAllowed() = runTest {
            val sdk = authorizedSdk()
            val otherSdk = altAuthorizedSdkDeferred.await()
            waitForTest {
                asyncSetup(object {
                    val party = stubParty()
                    val player = Player(
                        id = "${uuid4()}",
                        name = "Awesome-O",
                        callSignAdjective = "Awesome",
                        callSignNoun = "Sauce",
                        avatarType = null,
                    )
                }) {
                    otherSdk.partyRepository.save(party)
                } exercise {
                    sdk.playerRepository.save(party.id.with(player))
                    otherSdk.playerRepository.getPlayers(party.id)
                } verifyAnd { result ->
                    result.assertIsEqualTo(emptyList())
                } teardown {
                    otherSdk.partyRepository.deleteIt(party.id)
                }
            }
        }

        @Test
        fun deleteIsNotAllowed() = runTest {
            val sdk = authorizedSdk()
            waitForTest {
                asyncSetup(object {
                    val party = stubParty()
                }) exercise {
                    sdk.playerRepository.deletePlayer(party.id, "player id")
                } verify { result ->
                    result.assertIsEqualTo(false)
                }
            }
        }
    }
}

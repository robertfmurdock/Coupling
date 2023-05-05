
import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.pairassignmentdocument.RequestSpinAction
import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.repository.validation.assertHasIds
import com.zegreatrob.coupling.repository.validation.assertIsCloseToNow
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.testmints.async.waitForTest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SdkPlayerRepositoryTest {

    private val sdkSetup = asyncTestTemplate(
        sharedSetup = suspend {
            val authorizedSdk = authorizedSdk()
            object : Sdk by authorizedSdk {
                val party = stubParty()
                override suspend fun perform(action: RequestSpinAction) = authorizedSdk.perform(action)
                override suspend fun perform(query: UserQuery) = authorizedSdk.perform(query)
            }.apply {
                partyRepository.save(party)
            }
        },
        sharedTeardown = {
            it.partyRepository.deleteIt(it.party.id)
        },
    )

    @Test
    fun afterSavingPlayerTwiceGetWillReturnOnlyTheUpdatedPlayer() = sdkSetup.with({
        object {
            val sdk = it
            val player = stubPlayer()
            val updatedPlayer = player.copy(name = "Timmy!")
        }
    }) {
        sdk.save(sdk.party.id.with(this.player))
    } exercise {
        sdk.save(sdk.party.id.with(this.updatedPlayer))
        sdk.getPlayers(sdk.party.id)
    } verify { result ->
        result.map { it.data.player }
            .assertIsEqualTo(listOf(this.updatedPlayer))
    }

    @Test
    fun deleteWillRemoveAGivenPlayer() = sdkSetup.with({
        object {
            val sdk = it
            val partyId = it.party.id
            val player = stubPlayer()
        }
    }) {
        sdk.save(partyId.with(this.player))
    } exercise {
        sdk.deletePlayer(partyId, this.player.id)
        sdk.getPlayers(partyId)
    } verifyWithWait { result ->
        result.map { it.data.player }
            .contains(this.player)
            .assertIsEqualTo(false)
    }

    @Test
    fun deleteWithUnknownPlayerIdWillReturnFalse() = sdkSetup.with({
        object {
            val sdk = it
            val playerId = "${uuid4()}"
        }
    }) exercise {
        sdk.deletePlayer(sdk.party.id, this.playerId)
    } verify { result ->
        result.assertIsEqualTo(false)
    }

    @Test
    fun deletedPlayersShowUpInGetDeleted() = sdkSetup.with(
        {
            object {
                val sdk = it
                val partyId = sdk.party.id
                val player = stubPlayer()
            }
        },
    ) {
        sdk.save(partyId.with(this.player))
        sdk.deletePlayer(partyId, this.player.id)
    } exercise {
        sdk.getDeleted(partyId)
    } verify { result ->
        result.map { it.data.player }
            .assertIsEqualTo(listOf(this.player))
    }

    @Test
    fun deletedThenBringBackThenDeletedWillShowUpOnceInGetDeleted() = sdkSetup.with({
        object {
            val sdk = it
            val partyId = it.party.id
            val player = stubPlayer()
            val playerId = player.id
        }
    }) exercise {
        sdk.save(partyId.with(this.player))
        sdk.deletePlayer(partyId, this.playerId)
        sdk.save(partyId.with(this.player))
        sdk.deletePlayer(partyId, this.playerId)
    } verifyWithWait {
        sdk.getDeleted(this.partyId)
            .map { it.data.player }
            .assertIsEqualTo(listOf(this.player))
    }

    @Test
    fun saveMultipleInPartyThenGetListWillReturnSavedPlayers() = sdkSetup.with({
        object {
            val sdk = it
            val partyId = it.party.id
            val players = stubPlayers(3)
        }
    }) {
        partyId.with(this.players).forEach { sdk.save(it) }
    } exercise {
        sdk.getPlayers(partyId)
    } verify { result ->
        result.map { it.data.player }
            .assertIsEqualTo(this.players)
    }

    @Test
    fun saveWorksWithNullableValuesAndAssignsIds() = sdkSetup.with({
        object {
            val sdk = it
            val partyId = it.party.id
            val player = Player(
                name = "",
                email = "",
                callSignAdjective = "1",
                callSignNoun = "2",
                imageURL = null,
                avatarType = null,
            )
        }
    }) {
        sdk.save(partyId.with(this.player))
    } exercise {
        sdk.getPlayers(partyId)
    } verify { result ->
        result.map { it.data.player }
            .also { it.assertHasIds() }
            .assertIsEqualTo(listOf(this.player))
    }

    @Test
    fun whenPlayerIdIsUsedInTwoDifferentPartiesTheyRemainDistinct() = sdkSetup.with({
        object {
            val sdk = it.sdk
            val partyId = it.party.id
            val player1 = stubPlayer()
            val partyId2 = stubPartyId()
            val player2 = player1.copy(id = player1.id)
        }
    }) {
        sdk.partyRepository.save(stubParty().copy(id = partyId2))
        sdk.save(partyId.with(player1))
        sdk.save(partyId2.with(player2))
    } exercise {
        sdk.getPlayers(partyId)
    } verifyAnd { result ->
        result.map { it.data.player }
            .assertIsEqualTo(listOf(player1))
    } teardown {
        sdk.partyRepository.deleteIt(partyId2)
    }

    @Test
    fun deletedPlayersIncludeModificationDateAndUsername() = sdkSetup.with({
        object {
            val sdk = it
            val partyId = it.party.id
            val player = stubPlayer()
        }
    }) exercise {
        sdk.save(partyId.with(player))
        sdk.deletePlayer(partyId, player.id)
        sdk.getDeleted(partyId)
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            isDeleted.assertIsEqualTo(true)
            timestamp.assertIsCloseToNow()
            modifyingUserId.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
        }
    }

    @Test
    fun savedPlayersIncludeModificationDateAndUsername() = sdkSetup.with({
        object {
            val sdk = it
            val partyId = it.party.id
            val player = stubPlayer()
        }
    }) exercise {
        sdk.save(partyId.with(player))
        sdk.getPlayers(partyId)
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
                    otherSdk.save(party.id.with(stubPlayer()))
                } exercise {
                    sdk.getPlayers(party.id)
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
                    sdk.save(party.id.with(player))
                    otherSdk.getPlayers(party.id)
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
                    sdk.deletePlayer(party.id, "player id")
                } verify { result ->
                    result.assertIsEqualTo(false)
                }
            }
        }
    }
}

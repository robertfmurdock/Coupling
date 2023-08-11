package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.SaveBoostCommand
import com.zegreatrob.coupling.action.boost.DeleteBoostCommand
import com.zegreatrob.coupling.action.boost.fire
import com.zegreatrob.coupling.action.fire
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.minassert.assertIsEqualTo
import kotlinx.datetime.Instant
import kotlin.test.Test

class SdkBoostTest {

    private val setupWithUser = asyncSetup.extend(
        beforeAll = suspend {
            val sdk = sdk()
            val user = sdk.fire(graphQuery { user { details() } })?.user?.details
                ?: throw Exception("Sdk did not provide user.")
            object {
                val sdk = sdk
                val user = user
            }
        },
    )

    @Test
    fun deleteWillMakeBoostNotRecoverableThroughGet() = setupWithUser {
    } exercise {
        sdk.fire(SaveBoostCommand(setOf(PartyId("${uuid4()}"), PartyId("${uuid4()}"))))
        sdk.fire(DeleteBoostCommand())
    } verifyWithWait {
        sdk.fire(graphQuery { user { boost() } })
            ?.user
            ?.boost
            .assertIsEqualTo(null)
    }

    @Test
    fun getBoostWhenThereIsNoneReturnsNull() = setupWithUser {
    } exercise {
        sdk.fire(DeleteBoostCommand())
    } verifyWithWait {
        sdk.fire(graphQuery { user { boost() } })
            ?.user
            ?.boost
            .assertIsEqualTo(null)
    }

    @Test
    fun getSavedBoostViaUserWillReturnSuccessfully() = setupWithUser.with({
        object {
            val sdk = it.sdk
            val userId = it.user.id
            val partyIds = setOf(PartyId("${uuid4()}"), PartyId("${uuid4()}"))
        }
    }) exercise {
        sdk.fire(SaveBoostCommand(partyIds))
    } verifyWithWait {
        sdk.fire(graphQuery { user { boost() } })
            ?.user?.boost?.data?.copy(expirationDate = Instant.DISTANT_FUTURE)
            .assertIsEqualTo(
                Boost(
                    userId = userId,
                    partyIds = partyIds,
                    expirationDate = Instant.DISTANT_FUTURE,
                ),
            )
    }

    @Test
    fun getSavedBoostViaPartyWillReturnSuccessfully() = setupWithUser.with({
        object {
            val sdk = it.sdk
            val userId = it.user.id
            val party = stubPartyDetails()
            val partyIds = setOf(party.id)
        }
    }) exercise {
        sdk.fire(SavePartyCommand(party))
        sdk.fire(SaveBoostCommand(partyIds))
    } verifyWithWait {
        sdk.fire(graphQuery { party(party.id) { boost() } })
            ?.party?.boost?.data?.copy(expirationDate = Instant.DISTANT_FUTURE)
            .assertIsEqualTo(
                Boost(
                    userId = userId,
                    partyIds = partyIds,
                    expirationDate = Instant.DISTANT_FUTURE,
                ),
            )
    }

    @Test
    fun saveBoostRepeatedlyGetsLatest() = setupWithUser.with({
        object {
            val sdk = it.sdk
            val userId = it.user.id
            val initialBoostParties = setOf(PartyId("${uuid4()}"), PartyId("${uuid4()}"))
            val updatedBoostParties1 = emptySet<PartyId>()
            val updatedBoostParties2 = setOf(PartyId("${uuid4()}"))
        }
    }) exercise {
        sdk.fire(SaveBoostCommand(initialBoostParties))
        sdk.fire(SaveBoostCommand(updatedBoostParties1))
        sdk.fire(SaveBoostCommand(updatedBoostParties2))
    } verifyWithWait {
        sdk.fire(graphQuery { user { boost() } })
            ?.user?.boost?.data?.copy(expirationDate = Instant.DISTANT_FUTURE)
            .assertIsEqualTo(
                Boost(
                    userId = userId,
                    partyIds = updatedBoostParties2,
                    expirationDate = Instant.DISTANT_FUTURE,
                ),
            )
    }
}

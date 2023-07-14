package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.boost.BoostQuery
import com.zegreatrob.coupling.action.boost.DeleteBoostCommand
import com.zegreatrob.coupling.action.boost.SaveBoostCommand
import com.zegreatrob.coupling.action.boost.fire
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

class SdkBoostTest {

    private val setupWithUser = asyncSetup.extend(
        beforeAll = suspend {
            val sdk = sdk()
            val user = sdk.fire(graphQuery { user() })?.user
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
        fire(sdk, SaveBoostCommand(setOf(PartyId("${uuid4()}"), PartyId("${uuid4()}"))))
        fire(sdk, DeleteBoostCommand())
    } verifyWithWait {
        fire(sdk, BoostQuery())
            .assertIsEqualTo(null)
    }

    @Test
    fun getBoostWhenThereIsNoneReturnsNull() = setupWithUser {
    } exercise {
        fire(sdk, DeleteBoostCommand())
    } verifyWithWait {
        fire(sdk, BoostQuery())
            .assertIsEqualTo(null)
    }

    @Test
    fun getSavedBoostWillReturnSuccessfully() = setupWithUser.with({
        object {
            val sdk = it.sdk
            val userId = it.user.id
            val partyIds = setOf(PartyId("${uuid4()}"), PartyId("${uuid4()}"))
        }
    }) exercise {
        fire(sdk, SaveBoostCommand(partyIds))
    } verifyWithWait {
        fire(sdk, BoostQuery())
            ?.data
            .assertIsEqualTo(
                Boost(
                    userId = userId,
                    partyIds = partyIds,
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
        fire(sdk, SaveBoostCommand(initialBoostParties))
        fire(sdk, SaveBoostCommand(updatedBoostParties1))
        fire(sdk, SaveBoostCommand(updatedBoostParties2))
    } verifyWithWait {
        fire(sdk, BoostQuery())
            ?.data
            .assertIsEqualTo(
                Boost(
                    userId = userId,
                    partyIds = updatedBoostParties2,
                ),
            )
    }
}

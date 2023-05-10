package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

class SdkBoostRepositoryTest {

    private val setupWithUser = asyncTestTemplate(
        sharedSetup = suspend {
            val sdk = authorizedSdk()
            val user = sdk.perform(UserQuery())?.let { Record(it, "") }!!.data
            object : BarebonesSdk by sdk {
                val user = user
            }
        },
    )

    @Test
    fun deleteWillMakeBoostNotRecoverableThroughGet() = setupWithUser().exercise {
        save(Boost(user.id, setOf(PartyId("${uuid4()}"), PartyId("${uuid4()}"))))
        deleteIt()
    } verifyWithWait {
        get()
            .assertIsEqualTo(null)
    }

    @Test
    fun getBoostWhenThereIsNoneReturnsNull() = setupWithUser().exercise {
        deleteIt()
    } verifyWithWait {
        get()
            .assertIsEqualTo(null)
    }

    @Test
    fun getSavedBoostWillReturnSuccessfully() = setupWithUser.with({
        object : BarebonesSdk by it {
            val boost by lazy { Boost(it.user.id, setOf(PartyId("${uuid4()}"), PartyId("${uuid4()}"))) }
        }
    }) exercise {
        save(this.boost)
    } verifyWithWait {
        get()?.data
            .assertIsEqualTo(this.boost)
    }

    @Test
    fun saveBoostRepeatedlyGetsLatest() = setupWithUser.with({
        object : BarebonesSdk by it {
            val boost = Boost(it.user.id, setOf(PartyId("${uuid4()}"), PartyId("${uuid4()}")))
            val updatedBoost1 = boost.copy(partyIds = emptySet())
            val updatedBoost2 = updatedBoost1.copy(partyIds = setOf(PartyId("${uuid4()}")))
        }
    }) exercise {
        save(boost)
        save(updatedBoost1)
        save(updatedBoost2)
    } verifyWithWait {
        get()?.data
            .assertIsEqualTo(updatedBoost2)
    }
}

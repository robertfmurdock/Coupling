package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.soywiz.klock.seconds
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

@Suppress("unused")
val setJasmineTimeout = js("jasmine.DEFAULT_TIMEOUT_INTERVAL=10000")

class SdkPairAssignmentDocumentRepositoryTest : PairAssignmentDocumentRepositoryValidator<Sdk> {

    override val repositorySetup = asyncTestTemplate<TribeContext<Sdk>>(sharedSetup = {
        val user = stubUser().copy(email = "eT-user-${uuid4()}")
        val sdk = authorizedSdk(username = user.email)
        val tribe = stubTribe()
        sdk.save(tribe)
        TribeContextData(sdk, tribe.id, MagicClock(), user)
    })

    @Test
    fun givenNoAuthGetIsNotAllowed() = asyncSetup(contextProvider = {
        val sdk = authorizedSdk()
        val otherSdk = authorizedSdk(uuidString())
        object {
            val otherTribe = stubTribe()
            val sdk = sdk
            val otherSdk = otherSdk
        }
    }) {
        otherSdk.save(otherTribe)
        otherSdk.save(otherTribe.id.with(stubPairAssignmentDoc()))
    } exercise {
        sdk.getPairAssignments(TribeId("someoneElseTribe"))
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    override fun savedWillIncludeModificationDateAndUsername() = repositorySetup(object : TribeContextMint<Sdk>() {
        val pairAssignmentDoc = stubPairAssignmentDoc()
    }.bind()) {
        repository.save(tribeId.with(pairAssignmentDoc))
    } exercise {
        repository.getPairAssignments(tribeId)
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            timestamp.assertIsRecentDateTime()
            modifyingUserId.assertIsEqualTo(user.email)
        }
    }

    private fun DateTime.assertIsRecentDateTime() = (DateTime.now() - this)
        .compareTo(2.seconds)
        .assertIsEqualTo(-1)

}
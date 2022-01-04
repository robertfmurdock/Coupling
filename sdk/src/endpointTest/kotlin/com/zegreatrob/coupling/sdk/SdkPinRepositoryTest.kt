package com.zegreatrob.coupling.sdk

import com.soywiz.klock.DateTime
import com.zegreatrob.minassert.assertIsEqualTo

//class SdkPinRepositoryTest : PinRepositoryValidator<Sdk> {
//
//    override val repositorySetup = asyncTestTemplate<TribeContext<Sdk>>(sharedSetup = {
//        val username = "eT-user-${uuid4()}"
//        val sdk = authorizedKtorSdk(username = username)
//        val tribe = stubTribe()
//        sdk.save(tribe)
//        TribeContextData(sdk, tribe.id, MagicClock(), stubUser().copy(email = username))
//    })
//
//    @Test
//    fun givenNoAuthGetIsNotAllowed() = asyncSetup({
//        val sdk = authorizedKtorSdk()
//        val otherSdk = authorizedKtorSdk(uuidString())
//        object {
//            val otherTribe = stubTribe()
//            val sdk = sdk
//            val otherSdk = otherSdk
//        }
//    }) {
//        otherSdk.save(otherTribe)
//        otherSdk.save(otherTribe.id.with(stubPin()))
//    } exercise {
//        sdk.getPins(otherTribe.id)
//    } verify { result ->
//        result.assertIsEqualTo(emptyList())
//    }
//
//    override fun savedPinsIncludeModificationDateAndUsername() = repositorySetup(object : TribeContextMint<Sdk>() {
//        val pin = stubPin()
//    }.bind()) {
//        repository.save(tribeId.with(pin))
//    } exercise {
//        repository.getPins(tribeId)
//    } verify { result ->
//        result.size.assertIsEqualTo(1)
//        result.first().apply {
//            modifyingUserId.assertIsEqualTo(user.email)
//            timestamp.isWithinOneSecondOfNow()
//        }
//    }
//
//}

fun DateTime.isWithinOneSecondOfNow() {
    val timeSpan = DateTime.now() - this
    (timeSpan.seconds < 1)
        .assertIsEqualTo(true)
}

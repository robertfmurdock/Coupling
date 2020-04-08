package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.validation.TribeRepositoryValidator
import com.zegreatrob.coupling.sdk.SdkPlayerRepositoryTest.Companion.catchAxiosError
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.test.Test

class SdkTribeRepositoryTest : TribeRepositoryValidator {

    override suspend fun withRepository(clock: TimeProvider, handler: suspend (TribeRepository, User) -> Unit) {
        val email = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = email)
        handler(sdk, stubUser().copy(email = "$email._temp"))
    }

    @Test
    fun getWillReturnAnyTribeThatHasPlayerWithGivenEmail() = testAsync {
        val otherSdk = authorizedSdk(username = "eT-other-user-${uuid4()}")
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        setupAsync(object {
            val tribe = Tribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
            val player = Player(
                id = monk.id().toString(),
                name = "delete-me",
                email = "$username._temp"
            )
        }) {
            otherSdk.save(tribe)
            otherSdk.save(tribe.id.with(player))
        } exerciseAsync {
            sdk.getTribes()
        } verifyAsync { result ->
            result.map { it.data }
                .assertIsEqualTo(listOf(tribe))
        }
    }

    @Test
    fun getWillNotReturnTribeIfPlayerHadEmailButThenHadItRemoved() = testAsync {
        val otherSdk = authorizedSdk(username = "eT-other-user-${uuid4()}")
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        setupAsync(object {
            val tribe = Tribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
            val player = Player(
                id = monk.id().toString(),
                name = "delete-me",
                email = "$username._temp"
            )
        }) {
            otherSdk.save(tribe)
            otherSdk.save(tribe.id.with(player))
            otherSdk.save(tribe.id.with(player.copy(email = "something else")))
        } exerciseAsync {
            sdk.getTribes()
        } verifyAsync { result ->
            result.assertIsEqualTo(emptyList())
        }
    }

    @Test
    fun getWillNotReturnTribeIfPlayerHadEmailButPlayerWasRemoved() = testAsync {
        val otherSdk = authorizedSdk(username = "eT-other-user-${uuid4()}")
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        setupAsync(object {
            val tribe = Tribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
            val player = Player(
                id = monk.id().toString(),
                name = "delete-me",
                email = "$username._temp"
            )
        }) {
            otherSdk.save(tribe)
            otherSdk.save(tribe.id.with(player))
            otherSdk.deletePlayer(tribe.id, player.id!!)
        } exerciseAsync {
            sdk.getTribes()
        } verifyAsync { result ->
            result.assertIsEqualTo(emptyList())
        }
    }

    @Test
    fun postWillFailWhenTribeAlreadyExistsForSomeoneElse() = testAsync {
        val otherSdk = authorizedSdk(username = "eT-other-user-${uuid4()}")
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        setupAsync(object {
            val tribe = Tribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
        }) {
            otherSdk.save(tribe)
        } exerciseAsync {
            catchAxiosError {
                sdk.save(tribe)
            }
        } verifyAsync { result ->
            result["status"].assertIsEqualTo(400)
        }
    }

    override fun saveWillIncludeModificationInformation() = super.testRepository { repository, user, _ ->
        setupAsync(object {
            val tribe = stubTribe()
        }) {
            repository.save(tribe)
        } exerciseAsync {
            repository.getTribes()
        } verifyAsync { result ->
            result.first { it.data.id == tribe.id }.apply {
                modifyingUserId.assertIsEqualTo(user.email)
                timestamp.isWithinOneSecondOfNow()
            }
        }
    }

}
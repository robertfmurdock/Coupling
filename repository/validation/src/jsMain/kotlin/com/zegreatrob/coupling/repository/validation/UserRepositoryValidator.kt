package com.zegreatrob.coupling.repository.validation

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeProvider
import com.soywiz.klock.days
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.testAsync
import com.zegreatrob.testmints.async.waitForTest
import kotlin.test.Test
import kotlin.test.fail

interface UserRepositoryValidator {

    suspend fun withRepository(clock: TimeProvider, handler: suspend (UserRepository, User) -> Unit)

    private fun testRepository(
        block: (UserRepository, User, MagicClock) -> dynamic
    ) = testAsync {
        val clock = MagicClock()
        withRepository(clock) { repository, user -> waitForTest { block(repository, user, clock) } }
    }

    @Test
    fun getUserWillNotExplodeWhenUserDoesNotExistInDatabase() = testRepository { repository, _, _ ->
        asyncSetup(object {
        }) exercise {
            repository.getUser()
        } verify { result ->
            result.assertIsEqualTo(null)
        }
    }

    @Test
    fun getUsersWithEmailWillShowAllUsersWithEmail() = testRepository { repository, _, _ ->
        asyncSetup(object {
            val userWithEmail = stubUser()
        }) {
            repository.save(userWithEmail)
        } exercise {
            repository.getUsersWithEmail(userWithEmail.email)
        } verify { result ->
            result.map { it.data }
                .assertIsEqualTo(listOf(userWithEmail))
        }
    }

    @Test
    fun saveUserThenGetWillContainAllSavedValues() = testRepository { repository, user, _ ->
        asyncSetup(object {
            val updatedUser = user.copy(authorizedTribeIds = setOf(stubTribeId(), stubTribeId()))
        }) {
            repository.save(updatedUser)
        } exercise {
            repository.getUser()
        } verify { result ->
            result?.data
                .assertIsEqualTo(updatedUser)
        }
    }

    @Test
    fun saveUserThenGetWillIncludeMarkingInformation() = testRepository { repository, user, clock ->
        asyncSetup(object {
            val updatedUser = user.copy(authorizedTribeIds = setOf(stubTribeId(), stubTribeId()))
        }) {
            clock.currentTime = DateTime.now().plus(10.days)
            repository.save(updatedUser)
        } exercise {
            repository.getUser()
        } verify { result: Record<User>? ->
            if (result == null)
                fail()
            result.modifyingUserId.assertIsEqualTo(user.id)
            result.timestamp.assertIsEqualTo(clock.currentTime)
        }
    }

    @Test
    fun saveUserRepeatedlyGetsLatest() = testRepository { repository, user, _ ->
        asyncSetup(object {
            val updatedUser1 = user.copy(authorizedTribeIds = setOf(stubTribeId(), stubTribeId()))
            val updatedUser2 = user.copy(authorizedTribeIds = setOf(stubTribeId()))
        }) {
            repository.save(updatedUser1)
            repository.save(updatedUser2)
        } exercise {
            repository.getUser()
        } verify { result ->
            result?.data
                .assertIsEqualTo(updatedUser2)
        }
    }

}

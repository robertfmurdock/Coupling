package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test
import kotlin.test.fail
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

interface UserRepositoryValidator<R : UserRepository> : RepositoryValidator<R, SharedContext<R>> {

    @Test
    fun getUserWillNotExplodeWhenUserDoesNotExistInDatabase() = repositorySetup() exercise {
        repository.getUser()
    } verify { result ->
        result.assertIsEqualTo(null)
    }

    @Test
    fun getUsersWithEmailWillShowAllUsersWithEmail() = repositorySetup.with(
        object : ContextMint<R>() {
            val userWithEmail = stubUserDetails()
        }.bind(),
    ) exercise {
        repository.save(userWithEmail)
    } verifyWithWait {
        repository.getUsersWithEmail(userWithEmail.email)
            .map { it.data }
            .assertIsEqualTo(listOf(userWithEmail))
    }

    @Test
    fun saveUserThenGetWillContainAllSavedValues() = repositorySetup.with(
        object : ContextMint<R>() {
            val updatedUser by lazy { user.copy(authorizedPartyIds = setOf(stubPartyId(), stubPartyId())) }
        }.bind(),
    ) exercise {
        repository.save(updatedUser)
    } verifyWithWait {
        repository.getUser()!!
            .data
            .assertIsEqualTo(updatedUser)
    }

    @Test
    fun saveUserThenGetWillIncludeMarkingInformation() = repositorySetup.with(
        object : ContextMint<R>() {
            val updatedUser by lazy {
                user.copy(authorizedPartyIds = setOf(stubPartyId(), stubPartyId()), stripeCustomerId = null)
            }
        }.bind(),
    ) exercise {
        clock.currentTime = Clock.System.now().plus(10.days)
        repository.save(updatedUser)
    } verifyWithWait {
        val result = repository.getUser() ?: fail()
        result.modifyingUserId.assertIsEqualTo(user.id.value)
        result.timestamp.assertIsEqualTo(clock.currentTime)
    }

    @Test
    fun saveUserRepeatedlyGetsLatest() = repositorySetup.with({ sharedContext ->
        object : SharedContext<R> by sharedContext {
            val updatedUser1 = sharedContext.user.copy(authorizedPartyIds = setOf(stubPartyId(), stubPartyId()))
            val updatedUser2 = sharedContext.user.copy(authorizedPartyIds = setOf(stubPartyId()))
        }
    }) exercise {
        repository.save(updatedUser1)
        repository.save(updatedUser2)
    } verifyWithWait {
        repository.getUser()?.data
            .assertIsEqualTo(updatedUser2)
    }
}

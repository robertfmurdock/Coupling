package com.zegreatrob.coupling.repository.validation

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.invoke
import kotlin.test.Test
import kotlin.test.fail

interface UserRepositoryValidator<R : UserRepository> {

    val userRepositorySetup: TestTemplate<SharedContext<R>>

    @Test
    fun getUserWillNotExplodeWhenUserDoesNotExistInDatabase() = userRepositorySetup(contextProvider = { it }) exercise {
        repository.getUser()
    } verify { result ->
        result.assertIsEqualTo(null)
    }

    @Test
    fun getUsersWithEmailWillShowAllUsersWithEmail() = userRepositorySetup(contextProvider = object : ContextMint<R>() {
        val userWithEmail = stubUser()
    }.bind()) {
        repository.save(userWithEmail)
    } exercise {
        repository.getUsersWithEmail(userWithEmail.email)
    } verify { result ->
        result.map { it.data }
            .assertIsEqualTo(listOf(userWithEmail))
    }

    @Test
    fun saveUserThenGetWillContainAllSavedValues() = userRepositorySetup(contextProvider = object : ContextMint<R>() {
        val updatedUser by lazy { user.copy(authorizedTribeIds = setOf(stubTribeId(), stubTribeId())) }
    }.bind()) {
        repository.save(updatedUser)
    } exercise {
        repository.getUser()
    } verify { result ->
        result?.data
            .assertIsEqualTo(updatedUser)
    }

    @Test
    fun saveUserThenGetWillIncludeMarkingInformation() =
        userRepositorySetup(contextProvider = object : ContextMint<R>() {
            val updatedUser by lazy { user.copy(authorizedTribeIds = setOf(stubTribeId(), stubTribeId())) }
        }.bind()) {
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

    @Test
    fun saveUserRepeatedlyGetsLatest() = userRepositorySetup(contextProvider = { sharedContext ->
        object : ContextMint<R>() {
            val updatedUser1 = sharedContext.user.copy(authorizedTribeIds = setOf(stubTribeId(), stubTribeId()))
            val updatedUser2 = sharedContext.user.copy(authorizedTribeIds = setOf(stubTribeId()))
        }.bind()(sharedContext)
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

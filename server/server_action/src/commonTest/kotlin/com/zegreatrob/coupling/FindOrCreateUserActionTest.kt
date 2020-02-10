package com.zegreatrob.coupling

import SpyData
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserAction
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserActionDispatcher
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.test.Test
import kotlin.test.fail

class FindOrCreateUserActionTest {

    @Test
    fun whenUserDoesNotAlreadyExistWillCreate() = testAsync {
        setupAsync(object : FindOrCreateUserActionDispatcher, UserRepository {
            override val userRepository = this
            override val userEmail = "test@test.tes"

            override suspend fun getUser(): User? = null

            val saveSpy = SpyData<User, Unit>().apply { spyWillReturn(Unit) }
            override suspend fun save(user: User) = saveSpy.spyFunction(user)

        }) exerciseAsync {
            FindOrCreateUserAction.perform()
        } verifyAsync { result ->
            result.assertIsEqualTo(User(userEmail, emptySet()))
            saveSpy.spyReceivedValues.assertContains(result)
        }
    }

    @Test
    fun whenUserWithEmailExistsWillUseExistingUser() = testAsync {
        setupAsync(object : FindOrCreateUserActionDispatcher, UserRepository {
            override val userRepository = this
            override val userEmail = "test@test.tes"

            val expectedUser = User(userEmail, setOf(TribeId("Best tribe")))
            override suspend fun getUser() = expectedUser
            override suspend fun save(user: User) = fail("Should not save")

        }) exerciseAsync {
            FindOrCreateUserAction.perform()
        } verifyAsync { result ->
            result.assertIsEqualTo(expectedUser)
        }
    }


}
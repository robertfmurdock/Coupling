package com.zegreatrob.coupling

import SpyData
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserAction
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserActionDispatcher
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync2
import kotlin.test.Test
import kotlin.test.fail

class FindOrCreateUserActionTest {

    @Test
    fun whenUserDoesNotAlreadyExistWillCreate() =
        setupAsync2(object : FindOrCreateUserActionDispatcher, UserRepository {
            override val userRepository = this
            override val traceId: Uuid? = null
            override val userId = "test@test.tes"

            override suspend fun getUser(): Nothing? = null
            override suspend fun getUsersWithEmail(email: String): List<Record<User>> = emptyList()

            val saveSpy = SpyData<User, Unit>().apply { spyWillReturn(Unit) }
            override suspend fun save(user: User) = saveSpy.spyFunction(user)
        }) exercise {
            FindOrCreateUserAction.perform()
        } verify { result ->
            result.email.assertIsEqualTo(userId)
            result.authorizedTribeIds.assertIsEqualTo(emptySet())
            saveSpy.spyReceivedValues.assertContains(result)
        }

    @Test
    fun whenUserWithEmailAsIdExistsWillUseExistingUser() = setupAsync2(
        object : FindOrCreateUserActionDispatcher, UserRepository {
            override val userRepository = this
            override val traceId: Nothing? = null
            override val userId = "test@test.tes"

            val expectedUser = User("${uuid4()}", userId, setOf(TribeId("Best tribe")))
            override suspend fun getUser() = Record(expectedUser, "", false, DateTime.now())
            override suspend fun getUsersWithEmail(email: String): List<Record<User>> = emptyList()
            override suspend fun save(user: User) = fail("Should not save")
        }
    ) exercise {
        FindOrCreateUserAction.perform()
    } verify { result ->
        result.assertIsEqualTo(expectedUser)
    }

    @Test
    fun whenUserWithEmailAndDifferentIdExistsWillUseExistingUser() = setupAsync2(
        object : FindOrCreateUserActionDispatcher, UserRepository {
            override val userRepository = this
            override val traceId: Nothing? = null
            override val userId = "test@test.tes"

            val expectedUser = User("${uuid4()}", userId, setOf(TribeId("Best tribe")))
            override suspend fun getUser(): Nothing? = null
            override suspend fun getUsersWithEmail(email: String): List<Record<User>> =
                listOf(Record(expectedUser, "", false, DateTime.now()))

            override suspend fun save(user: User) = fail("Should not save")
        }
    ) exercise {
        FindOrCreateUserAction.perform()
    } verify { result ->
        result.assertIsEqualTo(expectedUser)
    }

}
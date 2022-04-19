package com.zegreatrob.coupling

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserAction
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserActionDispatcher
import com.zegreatrob.coupling.testaction.verifySuccess
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.fail

@ExperimentalCoroutinesApi
class FindOrCreateUserActionTest {

    @Test
    fun whenUserDoesNotAlreadyExistWillCreate() = asyncSetup(object : FindOrCreateUserActionDispatcher, UserRepository {
        override val userRepository = this
        override val userId = "test@test.tes"

        override suspend fun getUser(): Nothing? = null
        override suspend fun getUsersWithEmail(email: String): List<Record<User>> = emptyList()

        val saveSpy = SpyData<User, Unit>()
        override suspend fun save(user: User) = saveSpy.spyFunction(user)
    }) exercise {
        perform(FindOrCreateUserAction)
    } verifySuccess { result ->
        result.email.assertIsEqualTo(userId)
        result.authorizedPartyIds.assertIsEqualTo(emptySet())
        saveSpy.spyReceivedValues.assertContains(result)
    }

    @Test
    fun whenUserWithEmailAsIdExistsWillUseExistingUser() = asyncSetup(
        object : FindOrCreateUserActionDispatcher, UserRepository {
            override val userRepository = this
            override val userId = "test@test.tes"

            val expectedUser = User("${uuid4()}", userId, setOf(PartyId("Best party")))
            override suspend fun getUser() = Record(expectedUser, "", false, DateTime.now())
            override suspend fun getUsersWithEmail(email: String): List<Record<User>> = emptyList()
            override suspend fun save(user: User) = fail("Should not save")
        }
    ) exercise {
        perform(FindOrCreateUserAction)
    } verifySuccess { result ->
        result.assertIsEqualTo(expectedUser)
    }

    @Test
    fun whenUserWithEmailAndDifferentIdExistsWillUseExistingUser() = asyncSetup(object :
            FindOrCreateUserActionDispatcher, UserRepository {
            override val userRepository = this
            override val userId = "test@test.tes"

            val expectedUser = User("${uuid4()}", userId, setOf(PartyId("Best party")))
            override suspend fun getUser(): Nothing? = null
            override suspend fun getUsersWithEmail(email: String): List<Record<User>> =
                listOf(Record(expectedUser, "", false, DateTime.now()))

            override suspend fun save(user: User) = fail("Should not save")
        }) exercise {
        perform(FindOrCreateUserAction)
    } verifySuccess { result ->
        result.assertIsEqualTo(expectedUser)
    }
}

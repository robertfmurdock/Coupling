package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserAction
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserActionDispatcher
import com.zegreatrob.coupling.testaction.verifySuccess
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import kotools.types.text.NotBlankString
import kotlin.test.Test
import kotlin.test.fail
import kotlin.time.Clock

class FindOrCreateUserActionTest {

    @Test
    fun whenUserDoesNotAlreadyExistWillCreate() = asyncSetup(object : FindOrCreateUserActionDispatcher, UserRepository {
        override val userRepository = this
        override val userId = UserId.new()

        override suspend fun getUser(): Nothing? = null
        override suspend fun getUsersWithEmail(email: NotBlankString): List<Record<UserDetails>> = emptyList()

        val saveSpy = SpyData<UserDetails, Unit>()
        override suspend fun save(user: UserDetails) = saveSpy.spyFunction(user)
    }) exercise {
        perform(FindOrCreateUserAction)
    } verifySuccess { result ->
        result.email.assertIsEqualTo(userId.value)
        result.authorizedPartyIds.assertIsEqualTo(emptySet())
        saveSpy.spyReceivedValues.assertContains(result)
    }

    @Test
    fun whenUserWithEmailAsIdExistsWillUseExistingUser() = asyncSetup(
        object : FindOrCreateUserActionDispatcher, UserRepository {
            override val userRepository = this
            override val userId = UserId.new()

            val expectedUser = UserDetails(userId, userId.value, setOf(PartyId("Best party")), null)
            override suspend fun getUser() = Record(expectedUser, null, false, Clock.System.now())
            override suspend fun getUsersWithEmail(email: NotBlankString): List<Record<UserDetails>> = emptyList()
            override suspend fun save(user: UserDetails) = fail("Should not save")
        },
    ) exercise {
        perform(FindOrCreateUserAction)
    } verifySuccess { result ->
        result.assertIsEqualTo(expectedUser)
    }

    @Test
    fun whenUserWithEmailAndDifferentIdExistsWillUseExistingUser() = asyncSetup(object :
        FindOrCreateUserActionDispatcher, UserRepository {
        override val userRepository = this
        override val userId = UserId.new()

        val expectedUser = UserDetails(userId, userId.value, setOf(PartyId("Best party")), null)
        override suspend fun getUser(): Nothing? = null
        override suspend fun getUsersWithEmail(email: NotBlankString): List<Record<UserDetails>> = listOf(Record(expectedUser, null, false, Clock.System.now()))

        override suspend fun save(user: UserDetails) = fail("Should not save")
    }) exercise {
        perform(FindOrCreateUserAction)
    } verifySuccess { result ->
        result.assertIsEqualTo(expectedUser)
    }
}

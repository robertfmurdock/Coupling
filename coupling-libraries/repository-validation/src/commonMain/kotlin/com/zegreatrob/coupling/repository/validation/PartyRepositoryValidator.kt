package com.zegreatrob.coupling.repository.validation

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.coupling.stubmodel.stubParties
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test

@ExperimentalCoroutinesApi
interface PartyRepositoryValidator<R : PartyRepository> : RepositoryValidator<R, SharedContext<R>> {

    @Test
    fun saveMultipleThenGetListWillReturnSavedParties() = repositorySetup.with(
        object : ContextMint<R>() {
            val parties = stubParties(3)
        }.bind()
    ) {
        parties.forEach { repository.save(it) }
    } exercise {
        repository.getParties()
    } verify { result ->
        result.parties().assertContainsAll(parties)
    }

    private fun List<Party>.assertContainsAll(expectedParties: List<Party>) =
        expectedParties.forEach(this::assertContains)

    private fun List<Record<Party>>.parties() = map(Record<Party>::data)

    @Test
    fun saveMultipleThenGetEachByIdWillReturnSavedParties() = repositorySetup.with(
        object : ContextMint<R>() {
            val parties = stubParties(3)
        }.bind()
    ) {
        parties.forEach { repository.save(it) }
    } exercise {
        parties.map { repository.getPartyRecord(it.id)?.data }
    } verify { result ->
        result.assertIsEqualTo(parties)
    }

    @Test
    fun saveWillIncludeModificationInformation() = repositorySetup.with(
        object : ContextMint<R>() {
            val party = stubParty()
        }.bind()
    ) {
        clock.currentTime = DateTime.now().minus(3.days)
        repository.save(party)
    } exercise {
        repository.getParties()
    } verifyAnd { result ->
        result.first { it.data.id == party.id }.apply {
            modifyingUserId.assertIsEqualTo(user.email)
            timestamp.assertIsEqualTo(clock.currentTime)
        }
    } teardown {
        repository.delete(party.id)
    }

    @Test
    fun deleteWillMakePartyInaccessible() = repositorySetup.with(
        object : ContextMint<R>() {
            val party = stubParty()
        }.bind()
    ) {
        repository.save(party)
    } exercise {
        repository.delete(party.id)
        Pair(
            repository.getParties(),
            repository.getPartyRecord(party.id)?.data
        )
    } verifyAnd { (listResult, getResult) ->
        listResult.find { it.data.id == party.id }
            .assertIsEqualTo(null)
        getResult.assertIsEqualTo(null)
    } teardown {
        repository.delete(party.id)
    }
}

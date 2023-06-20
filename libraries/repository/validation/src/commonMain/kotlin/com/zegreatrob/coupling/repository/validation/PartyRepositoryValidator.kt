package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.coupling.stubmodel.stubParties
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPartyIntegration
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import korlibs.time.DateTime
import korlibs.time.days
import kotlin.test.Test

interface PartyRepositoryValidator<R : PartyRepository> : RepositoryValidator<R, SharedContext<R>> {

    @Test
    fun saveMultipleThenGetListWillReturnSavedParties() = repositorySetup.with(
        object : ContextMint<R>() {
            val parties = stubParties(3)
        }.bind(),
    ) {
        parties.forEach { repository.save(it) }
    } exercise {
        repository.loadParties()
    } verify { result ->
        result.parties().assertContainsAll(parties)
    }

    private fun List<PartyDetails>.assertContainsAll(expectedParties: List<PartyDetails>) =
        expectedParties.forEach(this::assertContains)

    private fun List<Record<PartyDetails>>.parties() = map(Record<PartyDetails>::data)

    @Test
    fun saveMultipleThenGetEachByIdWillReturnSavedParties() = repositorySetup.with(
        object : ContextMint<R>() {
            val parties = stubParties(3)
        }.bind(),
    ) {
        parties.forEach { repository.save(it) }
    } exercise {
        parties.map { repository.getDetails(it.id)?.data }
    } verify { result ->
        result.assertIsEqualTo(parties)
    }

    @Test
    fun saveWillIncludeModificationInformation() = repositorySetup.with(
        object : ContextMint<R>() {
            val party = stubPartyDetails()
        }.bind(),
    ) {
        clock.currentTime = DateTime.now().minus(3.days)
        repository.save(party)
    } exercise {
        repository.loadParties()
    } verifyAnd { result ->
        result.first { it.data.id == party.id }.apply {
            modifyingUserId.assertIsEqualTo(user.email)
            timestamp.assertIsEqualTo(clock.currentTime)
        }
    } teardown {
        repository.deleteIt(party.id)
    }

    @Test
    fun canSaveAndLoadIntegrationWithoutAffectingParty() = repositorySetup.with(
        object : ContextMint<R>() {
            val partyDetails = stubPartyDetails()
            val partyId = partyDetails.id
            val partyIntegration = stubPartyIntegration()
        }.bind(),
    ) {
        clock.currentTime = DateTime.now().minus(3.days)
        repository.save(partyDetails)
        repository.save(partyId.with(partyIntegration))
    } exercise {
        repository.getIntegration(partyId)
    } verifyAnd { result ->
        result?.data
            .assertIsEqualTo(partyIntegration)
        result!!.apply {
            modifyingUserId.assertIsEqualTo(user.email)
            timestamp.assertIsEqualTo(clock.currentTime)
        }
        repository.getDetails(partyId)?.data
            .assertIsEqualTo(partyDetails)
    } teardown {
        repository.deleteIt(partyDetails.id)
    }

    @Test
    fun deleteWillMakePartyInaccessible() = repositorySetup.with(
        object : ContextMint<R>() {
            val party = stubPartyDetails()
        }.bind(),
    ) {
        repository.save(party)
    } exercise {
        repository.deleteIt(party.id)
        Pair(
            repository.loadParties(),
            repository.getDetails(party.id)?.data,
        )
    } verifyAnd { (listResult, getResult) ->
        listResult.find { it.data.id == party.id }
            .assertIsEqualTo(null)
        getResult.assertIsEqualTo(null)
    } teardown {
        repository.deleteIt(party.id)
    }
}

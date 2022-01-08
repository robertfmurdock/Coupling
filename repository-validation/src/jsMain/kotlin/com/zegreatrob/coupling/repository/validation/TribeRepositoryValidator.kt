package com.zegreatrob.coupling.repository.validation

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubTribes
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

interface TribeRepositoryValidator<R : TribeRepository> : RepositoryValidator<R, SharedContext<R>> {

    @Test
    fun saveMultipleThenGetListWillReturnSavedTribes() = repositorySetup(object : ContextMint<R>() {
        val tribes = stubTribes(3)
    }.bind()) {
        tribes.forEach { repository.save(it) }
    } exercise {
        repository.getTribes()
    } verify { result ->
        result.tribes().assertContainsAll(tribes)
    }

    private fun List<Tribe>.assertContainsAll(expectedTribes: List<Tribe>) =
        expectedTribes.forEach(this::assertContains)

    private fun List<Record<Tribe>>.tribes() = map(Record<Tribe>::data)

    @Test
    fun saveMultipleThenGetEachByIdWillReturnSavedTribes() = repositorySetup(object : ContextMint<R>() {
        val tribes = stubTribes(3)
    }.bind()) {
        tribes.forEach { repository.save(it) }
    } exercise {
        tribes.map { repository.getTribeRecord(it.id)?.data }
    } verify { result ->
        result.assertIsEqualTo(tribes)
    }

    @Test
    fun saveWillIncludeModificationInformation() = repositorySetup(object : ContextMint<R>() {
        val tribe = stubTribe()
    }.bind()) {
        clock.currentTime = DateTime.now().minus(3.days)
        repository.save(tribe)
    } exercise {
        repository.getTribes()
    } verify { result ->
        result.first { it.data.id == tribe.id }.apply {
            modifyingUserId.assertIsEqualTo(user.email)
            timestamp.assertIsEqualTo(clock.currentTime)
        }
    }

    @Test
    fun deleteWillMakeTribeInaccessible() = repositorySetup(object : ContextMint<R>() {
        val tribe = stubTribe()
    }.bind()) {
        repository.save(tribe)
    } exercise {
        repository.delete(tribe.id)
        Pair(
            repository.getTribes(),
            repository.getTribeRecord(tribe.id)?.data
        )
    } verify { (listResult, getResult) ->
        listResult.find { it.data.id == tribe.id }
            .assertIsEqualTo(null)
        getResult.assertIsEqualTo(null)
    }

}

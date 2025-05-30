package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.repository.LiveInfoRepository
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test
import kotlin.uuid.Uuid

interface LiveInfoRepositoryValidator<R : LiveInfoRepository> : RepositoryValidator<R, SharedContext<R>> {

    @Test
    fun connectionListWillReturnLastSaved() = repositorySetup.with(
        object : ContextMint<R>() {
            val partyId = stubPartyId()
            val connections = listOf(
                CouplingConnection(Uuid.random().toString(), partyId, stubPlayer()),
                CouplingConnection(Uuid.random().toString(), partyId, stubPlayer()),
                CouplingConnection(Uuid.random().toString(), partyId, stubPlayer()),
            ).sortedBy { it.connectionId }
        }.bind(),
    ).exercise {
        connections.forEach { repository.save(it) }
    } verifyWithWait {
        repository.connectionList(partyId)
            .assertIsEqualTo(connections)
    }

    @Test
    fun getWillReturnConnection() = repositorySetup.with(
        object : ContextMint<R>() {
            val partyId = stubPartyId()
            val expectedConnection = CouplingConnection(Uuid.random().toString(), partyId, stubPlayer())
            val connections = listOf(
                CouplingConnection(Uuid.random().toString(), partyId, stubPlayer()),
                expectedConnection,
                CouplingConnection(Uuid.random().toString(), partyId, stubPlayer()),
            )
        }.bind(),
    ) exercise {
        connections.forEach { repository.save(it) }
    } verifyWithWait {
        repository.get(expectedConnection.connectionId)
            .assertIsEqualTo(expectedConnection)
    }

    @Test
    fun deleteWillMakeGetNoLongerReturnValue() = repositorySetup.with(
        object : ContextMint<R>() {
            val partyId = stubPartyId()
            val connections = listOf(
                CouplingConnection(Uuid.random().toString(), partyId, stubPlayer()),
                CouplingConnection(Uuid.random().toString(), partyId, stubPlayer()),
                CouplingConnection(Uuid.random().toString(), partyId, stubPlayer()),
            )
        }.bind(),
    ) {
        connections.forEach { repository.save(it) }
    } exercise {
        repository.deleteIt(partyId, connections[1].connectionId)
    } verifyWithWait {
        repository.connectionList(partyId)
            .assertIsEqualTo(listOf(connections[0], connections[2]).sortedBy { it.connectionId })
    }
}

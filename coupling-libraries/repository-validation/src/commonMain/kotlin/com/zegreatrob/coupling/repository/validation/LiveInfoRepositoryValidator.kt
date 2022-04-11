package com.zegreatrob.coupling.repository.validation

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.repository.LiveInfoRepository
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
interface LiveInfoRepositoryValidator<R : LiveInfoRepository> : RepositoryValidator<R, SharedContext<R>> {

    @Test
    fun connectionListWillReturnLastSaved() = repositorySetup.with(object : ContextMint<R>() {
        val tribeId = stubPartyId()
        val connections = listOf(
            CouplingConnection(uuid4().toString(), tribeId, stubPlayer()),
            CouplingConnection(uuid4().toString(), tribeId, stubPlayer()),
            CouplingConnection(uuid4().toString(), tribeId, stubPlayer())
        ).sortedBy { it.connectionId }
    }.bind()).exercise {
        connections.forEach { repository.save(it) }
    } verifyWithWait {
        repository.connectionList(tribeId)
            .assertIsEqualTo(connections)
    }

    @Test
    fun getWillReturnConnection() = repositorySetup.with(object : ContextMint<R>() {
        val tribeId = stubPartyId()
        val expectedConnection = CouplingConnection(uuid4().toString(), tribeId, stubPlayer())
        val connections = listOf(
            CouplingConnection(uuid4().toString(), tribeId, stubPlayer()),
            expectedConnection,
            CouplingConnection(uuid4().toString(), tribeId, stubPlayer())
        )
    }.bind()) exercise {
        connections.forEach { repository.save(it) }
    } verifyWithWait {
        repository.get(expectedConnection.connectionId)
            .assertIsEqualTo(expectedConnection)
    }

    @Test
    fun deleteWillMakeGetNoLongerReturnValue() = repositorySetup.with(object : ContextMint<R>() {
        val tribeId = stubPartyId()
        val connections = listOf(
            CouplingConnection(uuid4().toString(), tribeId, stubPlayer()),
            CouplingConnection(uuid4().toString(), tribeId, stubPlayer()),
            CouplingConnection(uuid4().toString(), tribeId, stubPlayer())
        )
    }.bind()) {
        connections.forEach { repository.save(it) }
    } exercise {
        repository.delete(tribeId, connections[1].connectionId)
    } verifyWithWait {
        repository.connectionList(tribeId)
            .assertIsEqualTo(listOf(connections[0], connections[2]).sortedBy { it.connectionId })
    }

}

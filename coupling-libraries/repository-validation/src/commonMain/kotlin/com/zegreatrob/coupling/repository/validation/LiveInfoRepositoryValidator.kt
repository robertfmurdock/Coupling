package com.zegreatrob.coupling.repository.validation

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.repository.LiveInfoRepository
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.minassert.assertIsEqualTo
import kotlinx.coroutines.delay
import kotlin.test.Test

interface LiveInfoRepositoryValidator<R : LiveInfoRepository> : RepositoryValidator<R, SharedContext<R>> {

    @Test
    fun connectionListWillReturnLastSaved() = repositorySetup.with(object : ContextMint<R>() {
        val tribeId = stubTribeId()
        val connections = listOf(
            CouplingConnection(uuid4().toString(), tribeId, stubPlayer()),
            CouplingConnection(uuid4().toString(), tribeId, stubPlayer()),
            CouplingConnection(uuid4().toString(), tribeId, stubPlayer())
        ).sortedBy { it.connectionId }
    }.bind()) {
        connections.forEach { repository.save(it) }
    } exercise {
        repository.connectionList(tribeId)
    } verify { result ->
        result.assertIsEqualTo(connections)
    }

    @Test
    fun getWillReturnConnection() = repositorySetup.with(object : ContextMint<R>() {
        val tribeId = stubTribeId()
        val expectedConnection = CouplingConnection(uuid4().toString(), tribeId, stubPlayer())
        val connections = listOf(
            CouplingConnection(uuid4().toString(), tribeId, stubPlayer()),
            expectedConnection,
            CouplingConnection(uuid4().toString(), tribeId, stubPlayer())
        )
    }.bind()) {
        connections.forEach { repository.save(it) }
    } exercise {
        repository.get(expectedConnection.connectionId)
    } verify { result ->
        result.assertIsEqualTo(expectedConnection)
    }

    @Test
    fun deleteWillMakeGetNoLongerReturnValue() = repositorySetup.with(object : ContextMint<R>() {
        val tribeId = stubTribeId()
        val connections = listOf(
            CouplingConnection(uuid4().toString(), tribeId, stubPlayer()),
            CouplingConnection(uuid4().toString(), tribeId, stubPlayer()),
            CouplingConnection(uuid4().toString(), tribeId, stubPlayer())
        )
    }.bind()) {
        connections.forEach { repository.save(it) }
    } exercise {
        repository.delete(tribeId, connections[1].connectionId)
        delay(30)
        repository.connectionList(tribeId)
    } verify { result ->
        result.assertIsEqualTo(listOf(connections[0], connections[2]).sortedBy { it.connectionId })
    }

}

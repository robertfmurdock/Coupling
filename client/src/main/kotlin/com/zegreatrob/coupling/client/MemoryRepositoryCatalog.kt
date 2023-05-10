package com.zegreatrob.coupling.client

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.action.boost.SaveBoostCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.RequestSpinAction
import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserIdSyntax
import com.zegreatrob.coupling.repository.memory.MemoryPairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.memory.MemoryPartyRepository
import com.zegreatrob.coupling.repository.memory.MemoryPinRepository
import com.zegreatrob.coupling.repository.memory.MemoryPlayerRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGetCurrent
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.coupling.repository.pin.PinGet
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.repository.player.PlayerListGet
import com.zegreatrob.coupling.repository.player.PlayerListGetDeleted
import com.zegreatrob.coupling.sdk.BarebonesSdk
import com.zegreatrob.coupling.sdk.ClientDeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.sdk.ClientDeletePartyCommandDispatcher
import com.zegreatrob.coupling.sdk.ClientDeletePinCommandDispatcher
import com.zegreatrob.coupling.sdk.ClientDeletePlayerCommandDispatcher
import com.zegreatrob.coupling.sdk.ClientSavePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.sdk.ClientSavePartyCommandDispatcher
import com.zegreatrob.coupling.sdk.ClientSavePinCommandDispatcher
import com.zegreatrob.coupling.sdk.ClientSavePlayerCommandDispatcher

class MemoryRepositoryCatalog private constructor(
    override val userId: String,
    override val clock: TimeProvider,
    override val partyRepository: PartyRepository,
    override val playerRepository: PlayerEmailRepository,
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository,
    override val pinRepository: PinRepository,
    override val traceId: Uuid = uuid4(),
) :
    BarebonesSdk,
    ClientSavePartyCommandDispatcher,
    ClientSavePairAssignmentsCommandDispatcher,
    ClientSavePlayerCommandDispatcher,
    ClientSavePinCommandDispatcher,
    ClientDeletePairAssignmentsCommandDispatcher,
    ClientDeletePlayerCommandDispatcher,
    ClientDeletePartyCommandDispatcher,
    ClientDeletePinCommandDispatcher,
    PlayerListGet by playerRepository,
    PlayerListGetDeleted by playerRepository,
    PinGet by pinRepository,
    PairAssignmentDocumentGetCurrent by pairAssignmentDocumentRepository,
    PairAssignmentDocumentGet by pairAssignmentDocumentRepository,
    UserIdSyntax,
    ClockSyntax {

    constructor(userEmail: String, backend: LocalStorageRepositoryBackend, clock: TimeProvider) : this(
        userEmail,
        clock,
        MemoryPartyRepository(userEmail, clock, backend.party),
        MemoryPlayerRepository(userEmail, clock, backend.player),
        MemoryPairAssignmentDocumentRepository(userEmail, clock, backend.pairAssignments),
        MemoryPinRepository(userEmail, clock, backend.pin),
    )

    override suspend fun perform(query: UserQuery) = User(userId, "???", setOf(PartyId("Kind of fake")))

    override suspend fun perform(action: RequestSpinAction): PairAssignmentDocument {
        val pairs = action.players.shuffled().map { it.withPins(emptyList()) }.withIndex().groupBy { it.index / 2 }
            .entries
            .map { it.value.map(IndexedValue<PinnedPlayer>::value) }
            .map { PinnedCouplingPair(it, emptySet()) }
            .toList()
        return PairAssignmentDocument(
            id = PairAssignmentDocumentId("${uuid4()}"),
            date = DateTime.now(),
            pairs = pairs,
        )
    }

    override suspend fun get() = TODO("Not yet implemented")
    override suspend fun deleteIt() = TODO("Not yet implemented")
    override suspend fun perform(command: SaveBoostCommand) = TODO("Not yet implemented")
}

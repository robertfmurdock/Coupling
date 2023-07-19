package com.zegreatrob.coupling.client.memory

import com.zegreatrob.coupling.action.GrantSlackAccessCommand
import com.zegreatrob.coupling.action.SpinCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.boost.BoostQuery
import com.zegreatrob.coupling.action.boost.DeleteBoostCommand
import com.zegreatrob.coupling.action.boost.SaveBoostCommand
import com.zegreatrob.coupling.action.party.SaveSlackIntegrationCommand
import com.zegreatrob.coupling.action.secret.CreateSecretCommand
import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.client.LocalStorageRepositoryBackend
import com.zegreatrob.coupling.client.party.NewPartyCommandDispatcher
import com.zegreatrob.coupling.json.PartyInput
import com.zegreatrob.coupling.model.ClockProvider
import com.zegreatrob.coupling.model.CouplingQueryResult
import com.zegreatrob.coupling.model.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.memory.MemoryPairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.memory.MemoryPartyRepository
import com.zegreatrob.coupling.repository.memory.MemoryPinRepository
import com.zegreatrob.coupling.repository.memory.MemoryPlayerRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGetCurrent
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.party.PartyDetailsGet
import com.zegreatrob.coupling.repository.party.PartyListGet
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.coupling.repository.pin.PinGet
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.repository.player.PlayerListGet
import com.zegreatrob.coupling.repository.player.PlayerListGetDeleted
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.gql.GraphQuery
import com.zegreatrob.testmints.action.ActionCannon
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class MemoryRepositoryCatalog private constructor(
    override val userId: String,
    override val clock: Clock,
    override val partyRepository: PartyRepository,
    override val playerRepository: PlayerEmailRepository,
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository,
    override val pinRepository: PinRepository,
) : CouplingSdkDispatcher,
    ClientDeletePairAssignmentsCommandDispatcher,
    ClientDeletePartyCommandDispatcher,
    ClientDeletePinCommandDispatcher,
    ClientDeletePlayerCommandDispatcher,
    ClientPartyListQueryDispatcher,
    ClientSavePairAssignmentsCommandDispatcher,
    ClientSavePartyCommandDispatcher,
    ClientSavePinCommandDispatcher,
    ClientSavePlayerCommandDispatcher,
    NewPartyCommandDispatcher,
    PartyDetailsGet by partyRepository,
    PartyListGet by partyRepository,
    PlayerListGet by playerRepository,
    PlayerListGetDeleted by playerRepository,
    PinGet by pinRepository,
    PairAssignmentDocumentGetCurrent by pairAssignmentDocumentRepository,
    PairAssignmentDocumentGet by pairAssignmentDocumentRepository,
    UserIdProvider,
    ClockProvider {

    constructor(userEmail: String, backend: LocalStorageRepositoryBackend, clock: Clock) : this(
        userEmail,
        clock,
        MemoryPartyRepository(userEmail, clock, backend.party),
        MemoryPlayerRepository(userEmail, clock, backend.player),
        MemoryPairAssignmentDocumentRepository(userEmail, clock, backend.pairAssignments),
        MemoryPinRepository(userEmail, clock, backend.pin),
    )

    override suspend fun perform(command: SpinCommand): SpinCommand.Result {
        TODO("Not yet implemented")
    }

    override suspend fun perform(command: BoostQuery) = TODO("Not yet implemented")
    override suspend fun perform(command: CreateSecretCommand): Pair<Secret, String> {
        TODO("Not yet implemented")
    }

    override suspend fun perform(command: DeleteSecretCommand): VoidResult {
        TODO("Not yet implemented")
    }

    override suspend fun perform(command: DeleteBoostCommand) = TODO("Not yet implemented")
    override suspend fun perform(command: SaveBoostCommand) = TODO("Not yet implemented")
    override suspend fun perform(command: SaveSlackIntegrationCommand): VoidResult {
        TODO("Not yet implemented")
    }

    override suspend fun perform(query: GraphQuery) = CouplingQueryResult(
        user = User(userId, "???", setOf(PartyId("Kind of fake"))),
        partyList = partyRepository.loadParties(),
        party = query.variables?.get("input")?.let { Json.decodeFromJsonElement<PartyInput>(it) }?.partyId?.let {
            val id = PartyId(it)
            Party(
                id,
                details = partyRepository.getDetails(id),
                playerList = playerRepository.getPlayers(id),
                pinList = pinRepository.getPins(id),
                pairAssignmentDocumentList = pairAssignmentDocumentRepository.loadPairAssignments(id),
                currentPairAssignmentDocument = pairAssignmentDocumentRepository.getCurrentPairAssignments(id),
            )
        },
    )

    override suspend fun perform(command: GrantSlackAccessCommand): VoidResult = VoidResult.Accepted

    val sdk: ActionCannon<CouplingSdkDispatcher> get() = ActionCannon(this)
}

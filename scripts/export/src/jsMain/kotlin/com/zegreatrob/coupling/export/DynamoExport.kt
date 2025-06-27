package com.zegreatrob.coupling.export

import com.zegreatrob.coupling.json.GqlPairAssignmentDocumentDetails
import com.zegreatrob.coupling.json.GqlPartyDetails
import com.zegreatrob.coupling.json.GqlPinDetails
import com.zegreatrob.coupling.json.GqlPlayerDetails
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toJsonDynamic
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.ClockProvider
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.dynamo.DynamoPairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoPartyRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoPinRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoPlayerRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoUserRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Json
import kotlin.js.json
import kotlin.time.Clock

fun exportWithDynamo() {
    MainScope().launch {
        val repositoryCatalog = DynamoRepositoryCatalog(user.id, Clock.System)
        outputUsers(repositoryCatalog)
        repositoryCatalog.outputParties()
    }
}

private suspend fun DynamoRepositoryCatalog.outputParties() = partyRepository.getPartyRecords()
    .groupBy { it.data.id }
    .entries.sortedBy { it.key.value }
    .forEach { partyGroup ->
        collectPartyData(this, partyGroup.key, partyGroup.value)
            .print()
    }

private suspend fun collectPartyData(
    repositoryCatalog: DynamoRepositoryCatalog,
    partyId: PartyId,
    partyRecords: List<Record<PartyDetails>>,
): Json = couplingJsonFormat.encodeToDynamic(
    partyDataSerializable(partyId, partyRecords, repositoryCatalog),
).unsafeCast<Json>()

private suspend fun partyDataSerializable(
    partyId: PartyId,
    partyRecords: List<Record<PartyDetails>>,
    repositoryCatalog: DynamoRepositoryCatalog,
) = PartyData(
    partyId = partyId.value.toString(),
    partyRecords = partyRecords.map(Record<PartyDetails>::toSerializable),
    playerRecords = repositoryCatalog.playerRepository.getPlayerRecords(partyId)
        .map(Record<PartyElement<Player>>::toSerializable),
    pairAssignmentRecords = repositoryCatalog.pairAssignmentDocumentRepository.getRecords(partyId)
        .map(PartyRecord<PairAssignmentDocument>::toSerializable),
    pinRecords = repositoryCatalog.pinRepository.getPinRecords(partyId)
        .map(Record<PartyElement<Pin>>::toSerializable),
)

@Serializable
data class PartyData(
    val partyId: String,
    val partyRecords: List<GqlPartyDetails>,
    val playerRecords: List<GqlPlayerDetails>,
    val pairAssignmentRecords: List<GqlPairAssignmentDocumentDetails>,
    val pinRecords: List<GqlPinDetails>,
)

private fun Json.print() = println(JSON.stringify(this))
private suspend fun outputUsers(repositoryCatalog: DynamoRepositoryCatalog) {
    repositoryCatalog.userRepository.getUserRecords()
        .groupBy { it.data.email }
        .entries.sortedBy { it.key }
        .forEach {
            json(
                "userEmail" to it.key,
                "userRecords" to it.value.map { record -> record.toSerializable().toJsonDynamic() },
            )
                .print()
        }
}

class DynamoRepositoryCatalog private constructor(
    override val userId: UserId,
    override val clock: Clock,
    val partyRepository: DynamoPartyRepository,
    val playerRepository: DynamoPlayerRepository,
    val pairAssignmentDocumentRepository: DynamoPairAssignmentDocumentRepository,
    val pinRepository: DynamoPinRepository,
    val userRepository: DynamoUserRepository,
) : UserIdProvider,
    ClockProvider {

    companion object {
        suspend operator fun invoke(userEmail: UserId, clock: Clock): DynamoRepositoryCatalog {
            val partyRepository = DynamoPartyRepository(userEmail, clock)
            val playerRepository = DynamoPlayerRepository(userEmail, clock)
            val pairAssignmentDocumentRepository = DynamoPairAssignmentDocumentRepository(userEmail, clock)
            val pinRepository = DynamoPinRepository(userEmail, clock)
            val userRepository = DynamoUserRepository(userEmail, clock)
            return DynamoRepositoryCatalog(
                userEmail,
                clock,
                partyRepository,
                playerRepository,
                pairAssignmentDocumentRepository,
                pinRepository,
                userRepository,
            )
        }
    }
}

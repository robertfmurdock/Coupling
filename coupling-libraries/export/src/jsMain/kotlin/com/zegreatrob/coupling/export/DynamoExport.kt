package com.zegreatrob.coupling.export

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.repository.dynamo.*
import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserIdSyntax
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Json
import kotlin.js.json

fun exportWithDynamo() {
    MainScope().launch {
        val repositoryCatalog = DynamoRepositoryCatalog(user.email, TimeProvider)
        outputUsers(repositoryCatalog)
        repositoryCatalog.outputTribes()
    }
}

private suspend fun DynamoRepositoryCatalog.outputTribes() = tribeRepository.getTribeRecords()
    .groupBy { it.data.id }
    .entries.sortedBy { it.key.value }
    .forEach { tribeGroup ->
        collectTribeData(this, tribeGroup.key, tribeGroup.value)
            .print()
    }

private suspend fun collectTribeData(
    repositoryCatalog: DynamoRepositoryCatalog,
    partyId: PartyId,
    partyRecords: List<Record<Party>>
) : Json = couplingJsonFormat.encodeToDynamic(
    tribeDataSerializable(partyId, partyRecords, repositoryCatalog)
).unsafeCast<Json>()

private suspend fun tribeDataSerializable(
    partyId: PartyId,
    partyRecords: List<Record<Party>>,
    repositoryCatalog: DynamoRepositoryCatalog
) = TribeData(
    tribeId = partyId.value,
    tribeRecords = partyRecords.map(Record<Party>::toSerializable),
    playerRecords = repositoryCatalog.playerRepository.getPlayerRecords(partyId)
        .map(Record<PartyElement<Player>>::toSerializable),
    pairAssignmentRecords = repositoryCatalog.pairAssignmentDocumentRepository.getRecords(partyId)
        .map(PartyRecord<PairAssignmentDocument>::toSerializable),
    pinRecords = repositoryCatalog.pinRepository.getPinRecords(partyId)
        .map(Record<PartyElement<Pin>>::toSerializable),
)

@Serializable
data class TribeData(
    val tribeId: String,
    val tribeRecords: List<JsonPartyRecord>,
    val playerRecords: List<JsonPlayerRecord>,
    val pairAssignmentRecords: List<JsonPairAssignmentDocumentRecord>,
    val pinRecords: List<JsonPinRecord>,
)

private fun Json.print() = println(JSON.stringify(this))
private suspend fun outputUsers(repositoryCatalog: DynamoRepositoryCatalog) {
    repositoryCatalog.userRepository.getUserRecords()
        .groupBy { it.data.email }
        .entries.sortedBy { it.key }
        .forEach {
            json("userEmail" to it.key, "userRecords" to it.value.map { it.toSerializable().toJsonDynamic() })
                .print()
        }
}

class DynamoRepositoryCatalog private constructor(
    override val userId: String,
    override val clock: TimeProvider,
    val tribeRepository: DynamoPartyRepository,
    val playerRepository: DynamoPlayerRepository,
    val pairAssignmentDocumentRepository: DynamoPairAssignmentDocumentRepository,
    val pinRepository: DynamoPinRepository,
    val userRepository: DynamoUserRepository
) :
    UserIdSyntax,
    ClockSyntax {

    companion object {
        suspend operator fun invoke(userEmail: String, clock: TimeProvider): DynamoRepositoryCatalog {
            val tribeRepository = DynamoPartyRepository(userEmail, clock)
            val playerRepository = DynamoPlayerRepository(userEmail, clock)
            val pairAssignmentDocumentRepository = DynamoPairAssignmentDocumentRepository(userEmail, clock)
            val pinRepository = DynamoPinRepository(userEmail, clock)
            val userRepository = DynamoUserRepository(userEmail, clock)
            return DynamoRepositoryCatalog(
                userEmail,
                clock,
                tribeRepository,
                playerRepository,
                pairAssignmentDocumentRepository,
                pinRepository,
                userRepository
            )
        }
    }

}

package com.zegreatrob.coupling.export

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.dynamo.*
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeElement
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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
    tribeId: TribeId,
    tribeRecords: List<Record<Tribe>>
) = json(
    "tribeId" to tribeId.value,
    "tribeRecords" to tribeRecords.map(Record<Tribe>::toJson),
    "playerRecords" to repositoryCatalog.playerRepository.getPlayerRecords(tribeId)
        .map(Record<TribeElement<Player>>::toJson),
    "pairAssignmentRecords" to repositoryCatalog.pairAssignmentDocumentRepository.getRecords(tribeId)
        .map(TribeRecord<PairAssignmentDocument>::toJson),
    "pinRecords" to repositoryCatalog.pinRepository.getPinRecords(tribeId).map(Record<TribeElement<Pin>>::toJson)
)

private fun Json.print() = println(JSON.stringify(this))
private suspend fun outputUsers(repositoryCatalog: DynamoRepositoryCatalog) {
    repositoryCatalog.userRepository.getUserRecords()
        .groupBy { it.data.email }
        .entries.sortedBy { it.key }
        .forEach {
            json("userEmail" to it.key, "userRecords" to it.value.map(Record<User>::toJson))
                .print()
        }
}

class DynamoRepositoryCatalog private constructor(
    override val userId: String,
    override val clock: TimeProvider,
    val tribeRepository: DynamoTribeRepository,
    val playerRepository: DynamoPlayerRepository,
    val pairAssignmentDocumentRepository: DynamoPairAssignmentDocumentRepository,
    val pinRepository: DynamoPinRepository,
    val userRepository: DynamoUserRepository
) :
    UserEmailSyntax,
    ClockSyntax {

    companion object {
        suspend operator fun invoke(userEmail: String, clock: TimeProvider): DynamoRepositoryCatalog {
            val tribeRepository = DynamoTribeRepository(userEmail, clock)
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

package com.zegreatrob.coupling.import

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.repository.dynamo.*
import com.zegreatrob.coupling.import.external.readline.ReadLine
import com.zegreatrob.coupling.import.external.readline.inputReader
import com.zegreatrob.coupling.import.external.readline.onEnd
import com.zegreatrob.coupling.import.external.readline.onNewLine
import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserIdSyntax
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.decodeFromDynamic
import mu.KotlinLogging
import mu.KotlinLoggingConfiguration
import mu.KotlinLoggingLevel
import kotlin.js.Json

val user = User("IMPORT_USER", "robert.f.murdock@gmail.com", emptySet())

private val logger = KotlinLogging.logger("import")

fun main() {
    KotlinLoggingConfiguration.LOG_LEVEL = KotlinLoggingLevel.WARN
    MainScope().launch {
        val catalog = DynamoRepositoryCatalog(user.email, TimeProvider)

        val reader = inputReader()
        reader.onNewLine { line ->
            val jsonLine = JSON.parse<Json>(line)
            when {
                jsonLine["userEmail"] != null -> launch { loadUser(jsonLine, catalog.userRepository) }
                jsonLine["tribeId"] != null -> launch { loadTribeData(jsonLine, catalog) }
            }
        }
        reader.inputStreamEnd().await()
    }
}

private fun ReadLine.inputStreamEnd() = CompletableDeferred<Unit>().also { endDeferred ->
    onEnd { endDeferred.complete(Unit) }
}

private val format = couplingJsonFormat

suspend fun loadTribeData(jsonLine: Json, catalog: DynamoRepositoryCatalog) {
    val partyId = jsonLine["tribeId"].unsafeCast<String>().let(::PartyId)
    jsonLine.getArray("tribeRecords").forEach { recordJson ->
        tryToImport({ "Failed to save tribe $partyId" }) {
            catalog.tribeRepository.saveRawRecord(
                format.decodeFromDynamic<JsonPartyRecord>(recordJson).toModelRecord()
            )
        }
    }
    jsonLine.getArray("playerRecords").forEach { recordJson ->
        val record = format.decodeFromDynamic<JsonPlayerRecord>(recordJson).toModel()
        tryToImport({ "Failed to save player ${record.data.id} in tribe $partyId" }) {
            catalog.playerRepository.saveRawRecord(record)
        }
    }
    jsonLine.getArray("pinRecords").forEach { recordJson ->
        val record = format.decodeFromDynamic<JsonPinRecord>(recordJson).toModel()
        tryToImport({ "Failed to save pin ${record.data.id} in tribe $partyId" }) {
            catalog.pinRepository.saveRawRecord(record)
        }
    }
    jsonLine.getArray("pairAssignmentRecords").forEach { recordJson ->
        val record = format.decodeFromDynamic<JsonPairAssignmentDocumentRecord>(recordJson).toModel()
        tryToImport({ "Failed to save player ${record.data.id} in tribe $partyId" }) {
            catalog.pairAssignmentDocumentRepository.saveRawRecord(record)
        }
    }
}

private inline fun tryToImport(noinline errorMessage: () -> Any?, suspend: () -> Unit) {
    try {
        suspend()
    } catch (bad: Throwable) {
        logger.error(bad, errorMessage)
    }
}

private fun Json.getArray(attributeName: String) = this[attributeName].unsafeCast<Array<Json>>()

private suspend fun loadUser(userJson: Json, userRepository: DynamoUserRepository) {
    logger.info { "LOADING USER ${userJson["userEmail"]}" }

    userJson["userRecords"].unsafeCast<Array<Json>>().forEach { recordJson ->
        userRepository.saveRawRecord(format.decodeFromDynamic<JsonUserRecord>(recordJson.asDynamic()).toModel())
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
) : UserIdSyntax, ClockSyntax {

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

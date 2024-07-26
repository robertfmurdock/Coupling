package com.zegreatrob.coupling.import

import com.zegreatrob.coupling.import.external.readline.ReadLine
import com.zegreatrob.coupling.import.external.readline.inputReader
import com.zegreatrob.coupling.import.external.readline.onEnd
import com.zegreatrob.coupling.import.external.readline.onNewLine
import com.zegreatrob.coupling.json.GqlPartyDetails
import com.zegreatrob.coupling.json.JsonPairAssignmentDocumentRecord
import com.zegreatrob.coupling.json.JsonPinRecord
import com.zegreatrob.coupling.json.JsonPlayerRecord
import com.zegreatrob.coupling.json.JsonUserRecord
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toModelRecord
import com.zegreatrob.coupling.model.ClockProvider
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.dynamo.DynamoPairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoPartyRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoPinRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoPlayerRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoUserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.decodeFromDynamic
import kotlin.js.Json

val user = UserDetails("IMPORT_USER", "robert.f.murdock@gmail.com", emptySet(), null)

private val logger = KotlinLogging.logger("import")

fun main() {
    KotlinLoggingConfiguration.logLevel = Level.WARN
    MainScope().launch {
        val catalog = DynamoRepositoryCatalog(user.email, Clock.System)

        val reader = inputReader()
        reader.onNewLine { line ->
            val jsonLine = JSON.parse<Json>(line)
            when {
                jsonLine["userEmail"] != null -> launch { loadUser(jsonLine, catalog.userRepository) }
                jsonLine["partyId"] != null -> launch { loadPartyData(jsonLine, catalog) }
            }
        }
        reader.inputStreamEnd().await()
    }
}

private fun ReadLine.inputStreamEnd() = CompletableDeferred<Unit>().also { endDeferred ->
    onEnd { endDeferred.complete(Unit) }
}

private val format = couplingJsonFormat

suspend fun loadPartyData(jsonLine: Json, catalog: DynamoRepositoryCatalog) {
    val partyId = jsonLine["partyId"].unsafeCast<String>().let(::PartyId)
    jsonLine.getArray("partyRecords").forEach { recordJson ->
        tryToImport({ "Failed to save party $partyId" }) {
            val record = format.decodeFromDynamic<GqlPartyDetails>(recordJson).toModelRecord()
            if (record != null) {
                catalog.partyRepository.saveRawRecord(record)
            }
        }
    }
    jsonLine.getArray("playerRecords").forEach { recordJson ->
        val record = format.decodeFromDynamic<JsonPlayerRecord>(recordJson).toModel()
        tryToImport({ "Failed to save player ${record.data.partyId} in party $partyId" }) {
            catalog.playerRepository.saveRawRecord(record)
        }
    }
    jsonLine.getArray("pinRecords").forEach { recordJson ->
        val record = format.decodeFromDynamic<JsonPinRecord>(recordJson).toModel()
        tryToImport({ "Failed to save pin ${record.data.partyId} in party $partyId" }) {
            catalog.pinRepository.saveRawRecord(record)
        }
    }
    jsonLine.getArray("pairAssignmentRecords").forEach { recordJson ->
        val record = format.decodeFromDynamic<JsonPairAssignmentDocumentRecord>(recordJson).toModel()
        tryToImport({ "Failed to save player ${record.data.partyId} in party $partyId" }) {
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
    override val clock: Clock,
    val partyRepository: DynamoPartyRepository,
    val playerRepository: DynamoPlayerRepository,
    val pairAssignmentDocumentRepository: DynamoPairAssignmentDocumentRepository,
    val pinRepository: DynamoPinRepository,
    val userRepository: DynamoUserRepository,
) : UserIdProvider,
    ClockProvider {

    companion object {
        suspend operator fun invoke(userEmail: String, clock: Clock): DynamoRepositoryCatalog {
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

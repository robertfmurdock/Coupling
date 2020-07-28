package com.zegreatrob.coupling.import

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.dynamo.*
import com.zegreatrob.coupling.import.external.readline.ReadLine
import com.zegreatrob.coupling.import.external.readline.inputReader
import com.zegreatrob.coupling.import.external.readline.onEnd
import com.zegreatrob.coupling.import.external.readline.onNewLine
import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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

suspend fun loadTribeData(jsonLine: Json, catalog: DynamoRepositoryCatalog) {
    val tribeId = jsonLine["tribeId"].unsafeCast<String>().let(::TribeId)
    jsonLine.getArray("tribeRecords").forEach { recordJson ->
        val tribe = recordJson.toTribe()
        tryToImport({ "Failed to save tribe $tribeId" }) {
            catalog.tribeRepository.saveRawRecord(
                recordJson.recordFor(tribe)
            )
        }
    }
    jsonLine.getArray("playerRecords").forEach { recordJson ->
        val player = recordJson.toPlayer()
        tryToImport({ "Failed to save player ${player.id} in tribe $tribeId" }) {
            catalog.playerRepository.saveRawRecord(
                recordJson.recordFor(tribeId.with(player))
            )
        }
    }
    jsonLine.getArray("pinRecords").forEach { recordJson ->
        val pin = recordJson.toPin()
        tryToImport({ "Failed to save pin ${pin.id} in tribe $tribeId" }) {
            catalog.pinRepository.saveRawRecord(
                recordJson.recordFor(tribeId.with(pin))
            )
        }
    }
    jsonLine.getArray("pairAssignmentRecords").forEach { recordJson ->
        val document = recordJson.toPairAssignmentDocument()
        tryToImport({ "Failed to save player ${document.id} in tribe $tribeId" }) {
            catalog.pairAssignmentDocumentRepository.saveRawRecord(
                recordJson.recordFor(tribeId.with(document))
            )
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
        userRepository.saveRawRecord(
            recordJson.recordFor(recordJson.toUser())
        )
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
) : UserEmailSyntax, ClockSyntax {

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

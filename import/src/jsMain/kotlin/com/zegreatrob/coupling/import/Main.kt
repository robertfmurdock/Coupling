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
import kotlin.js.Json

val user = User("IMPORT_USER", "robert.f.murdock@gmail.com", emptySet())

fun main() {
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
        catalog.tribeRepository.saveRawRecord(
            recordJson.recordFor(recordJson.toTribe())
        )
    }
    jsonLine.getArray("playerRecords").forEach { recordJson ->
        catalog.playerRepository.saveRawRecord(
            recordJson.recordFor(tribeId.with(recordJson.toPlayer()))
        )
    }
    jsonLine.getArray("pinRecords").forEach { recordJson ->
        catalog.pinRepository.saveRawRecord(
            recordJson.recordFor(tribeId.with(recordJson.toPin()))
        )
    }
    jsonLine.getArray("pairAssignmentRecords").forEach { recordJson ->
        catalog.pairAssignmentDocumentRepository.saveRawRecord(
            recordJson.recordFor(tribeId.with(recordJson.toPairAssignmentDocument()))
        )
    }
}

private fun Json.getArray(attributeName: String) = this[attributeName].unsafeCast<Array<Json>>()

private suspend fun loadUser(userJson: Json, userRepository: DynamoUserRepository) {
    println("LOADING USER ${userJson["userEmail"]}")

    userJson["userRecords"].unsafeCast<Array<Json>>().forEach { recordJson ->
        userRepository.saveRawRecord(
            recordJson.recordFor(recordJson.toUser())
        )
    }

}

class DynamoRepositoryCatalog private constructor(
    override val userEmail: String,
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

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.dynamo.*
import com.zegreatrob.coupling.export.external.readline.inputReader
import com.zegreatrob.coupling.export.external.readline.onEnd
import com.zegreatrob.coupling.export.external.readline.onNewLine
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.user.UserRepository
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
            if (jsonLine["userEmail"] != null) {
                launch { loadUser(jsonLine, catalog.userRepository) }
            }
        }
        val endDeferred = CompletableDeferred<Unit>()
        reader.onEnd { endDeferred.complete(Unit) }
        endDeferred.await()
    }
}

private suspend fun loadUser(userJson: Json, userRepository: UserRepository) {
    println("LOADING USER ${userJson["userEmail"]}")
}

class DynamoRepositoryCatalog private constructor(
    override val userEmail: String,
    override val clock: TimeProvider,
    val tribeRepository: TribeRepository,
    val playerRepository: PlayerEmailRepository,
    val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository,
    val pinRepository: PinRepository,
    val userRepository: UserRepository
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

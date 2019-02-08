import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.json

@Suppress("unused")
@JsName("performRunGameAction")
fun RunGameActionDispatcher.performRunGameAction(history: Array<Json>, players: Array<Json>, pins: Array<Json>, tribe: Json) =
        RunGameAction(
                players = players.map { it.toPlayer() }.toList(),
                pins = pins.toPins(),
                history = historyFromArray(history),
                tribe = tribe.toTribe()
        )
                .perform()
                .let { it.toJson() }

interface CommandDispatcher : ProposeNewPairsCommandDispatcher,
        PlayersQueryDispatcher,
        SavePlayerCommandDispatcher,
        DeletePlayerCommandDispatcher

@Suppress("unused")
@JsName("commandDispatcher")
fun commandDispatcher(jsRepository: dynamic, username: String): CommandDispatcher = object : CommandDispatcher,
        RunGameActionDispatcher,
        FindNewPairsActionDispatcher,
        NextPlayerActionDispatcher,
        CreatePairCandidateReportsActionDispatcher,
        CreatePairCandidateReportActionDispatcher,
        SavePairAssignmentDocumentCommandDispatcher,
        PairAssignmentDocumentListQueryDispatcher,
        DeletePairAssignmentDocumentCommandDispatcher,
        Wheel,
        MongoDataRepository,
        MongoPlayerRepository,
        MongoPairAssignmentDocumentRepository {
    override val pairAssignmentDocumentRepository = this
    override val repository = this
    override val jsRepository = jsRepository
    override val playerRepository = this
    override val userContext = object : UserContext {
        override val username = username
    }
    override val actionDispatcher = this
    override val wheel: Wheel = this

    @JsName("performSavePlayerCommand")
    fun performSavePlayerCommand(player: Json, tribeId: String) = GlobalScope.promise {
        SavePlayerCommand(TribeIdPlayer(player.toPlayer(), TribeId(tribeId)))
                .perform()
                .let { it.toJson() }
    }

    @JsName("performDeletePlayerCommand")
    fun performDeletePlayerCommand(playerId: String) = GlobalScope.promise {
        DeletePlayerCommand(playerId)
                .perform()
                .let { json() }
    }

    @JsName("performPlayersQuery")
    fun performPlayersQuery(tribeId: String) = GlobalScope.promise {
        PlayersQuery(TribeId(tribeId))
                .perform()
                .map { it.toJson() }
                .toTypedArray()
    }

    @JsName("performProposeNewPairsCommand")
    fun performProposeNewPairsCommand(tribeId: String, players: Array<Json>) = GlobalScope.promise {
        ProposeNewPairsCommand(TribeId(tribeId), players.map(Json::toPlayer))
                .perform()
                .let { it.toJson() }
    }

    @JsName("performSavePairAssignmentDocumentCommand")
    fun performSavePairAssignmentDocumentCommand(tribeId: String, pairAssignmentDocument: Json) = GlobalScope.promise {
        SavePairAssignmentDocumentCommand(TribeIdPairAssignmentDocument(TribeId(tribeId), pairAssignmentDocument.toPairAssignmentDocument()))
                .perform()
                .document
                .toJson()
    }

    @JsName("performDeletePairAssignmentDocumentCommand")
    fun performDeletePairAssignmentDocumentCommand(id: String) = GlobalScope.promise {
        DeletePairAssignmentDocumentCommand(id.let(::PairAssignmentDocumentId))
                .perform()
    }

    @JsName("performPairAssignmentDocumentListQuery")
    fun performPairAssignmentDocumentListQuery(tribeId: String) = GlobalScope.promise {
        PairAssignmentDocumentListQuery(TribeId(tribeId))
                .perform()
                .map { it.toJson() }
                .toTypedArray()
    }
}
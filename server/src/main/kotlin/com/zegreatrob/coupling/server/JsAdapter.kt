package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.TribeIdPlayer
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toJson
import com.zegreatrob.coupling.common.toPairAssignmentDocument
import com.zegreatrob.coupling.common.toPlayer
import com.zegreatrob.coupling.entity.pairassignmentdocument.*
import com.zegreatrob.coupling.entity.player.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.json

interface CommandDispatcher : ProposeNewPairsCommandDispatcher,
        PlayersQueryDispatcher,
        RetiredPlayersQueryDispatcher,
        SavePlayerCommandDispatcher,
        DeletePlayerCommandDispatcher,
        SavePairAssignmentDocumentCommandDispatcher,
        PairAssignmentDocumentListQueryDispatcher,
        DeletePairAssignmentDocumentCommandDispatcher {
    override val playerRepository: PlayerRepository
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}

@Suppress("unused")
@JsName("commandDispatcher")
fun commandDispatcher(jsRepository: dynamic, username: String): CommandDispatcher = object : CommandDispatcher,
        RunGameActionDispatcher,
        FindNewPairsActionDispatcher,
        NextPlayerActionDispatcher,
        CreatePairCandidateReportsActionDispatcher,
        CreatePairCandidateReportActionDispatcher,
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
        SavePlayerCommand(TribeIdPlayer(TribeId(tribeId), player.toPlayer()))
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

    @JsName("performRetiredPlayersQuery")
    fun performRetiredPlayersQuery(tribeId: String) = GlobalScope.promise {
        RetiredPlayersQuery(TribeId(tribeId))
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
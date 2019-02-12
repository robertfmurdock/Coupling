package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.UserContext
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.TribeIdPlayer
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toJson
import com.zegreatrob.coupling.common.toPairAssignmentDocument
import com.zegreatrob.coupling.common.toPlayer
import com.zegreatrob.coupling.common.toTribe
import com.zegreatrob.coupling.entity.PinRepository
import com.zegreatrob.coupling.entity.pairassignmentdocument.*
import com.zegreatrob.coupling.entity.player.*
import com.zegreatrob.coupling.entity.tribe.*
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
        DeletePairAssignmentDocumentCommandDispatcher,
        TribeListQueryDispatcher,
        TribeQueryDispatcher,
        SaveTribeCommandDispatcher {
    override val playerRepository: PlayerRepository
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}

interface RepositoryCatalog {
    val tribeRepository: TribeRepository
    val playerRepository: PlayerRepository
    val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
    val pinRepository: PinRepository
}

private fun repositoryCatalog(jsRepository: dynamic, userContext: UserContext) = object : RepositoryCatalog,
        MongoTribeRepository,
        MongoPlayerRepository,
        MongoPairAssignmentDocumentRepository,
        MongoPinRepository {
    override val jsRepository: dynamic = jsRepository
    override val userContext = userContext
    override val tribeRepository = this
    override val playerRepository = this
    override val pairAssignmentDocumentRepository = this
    override val pinRepository = this
}

@Suppress("unused")
@JsName("commandDispatcher")
fun commandDispatcher(jsRepository: dynamic, userEmail: String, tribeIds: Array<String>): CommandDispatcher {
    val userContext = userContext(tribeIds, userEmail)

    return object : CommandDispatcher,
            RunGameActionDispatcher,
            FindNewPairsActionDispatcher,
            NextPlayerActionDispatcher,
            CreatePairCandidateReportsActionDispatcher,
            CreatePairCandidateReportActionDispatcher,
            Wheel,
            RepositoryCatalog by repositoryCatalog(jsRepository, userContext) {
        override val userContext = userContext
        override val actionDispatcher = this
        override val wheel: Wheel = this

        @JsName("performTribeListQuery")
        fun performTribeListQuery() = GlobalScope.promise {
            TribeListQuery
                    .perform()
                    .map { it.toJson() }
                    .toTypedArray()
        }

        @JsName("performTribeQuery")
        fun performTribeQuery(tribeId: String) = GlobalScope.promise {
            TribeQuery(TribeId(tribeId))
                    .perform()
                    ?.toJson()
        }

        @JsName("performSaveTribeCommand")
        fun performSaveTribeCommand(tribe: Json) = GlobalScope.promise {
            SaveTribeCommand(tribe.toTribe())
                    .perform()
        }

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
}

private fun userContext(tribeIds: Array<String>, userEmail: String) = object : UserContext {
    override val tribeIds = tribeIds?.toList() ?: emptyList()
    override val userEmail = userEmail
}
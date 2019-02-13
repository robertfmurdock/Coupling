package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.AuthenticatedUserEmailSyntax
import com.zegreatrob.coupling.UserEmailSyntax
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.TribeIdPlayer
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.common.entity.user.User
import com.zegreatrob.coupling.common.entity.user.UserRepository
import com.zegreatrob.coupling.common.toJson
import com.zegreatrob.coupling.common.toPairAssignmentDocument
import com.zegreatrob.coupling.common.toPlayer
import com.zegreatrob.coupling.common.toTribe
import com.zegreatrob.coupling.server.entity.PinRepository
import com.zegreatrob.coupling.server.entity.UserIsAuthorizedAction
import com.zegreatrob.coupling.server.entity.UserIsAuthorizedActionDispatcher
import com.zegreatrob.coupling.server.entity.pairassignmentdocument.*
import com.zegreatrob.coupling.server.entity.player.*
import com.zegreatrob.coupling.server.entity.tribe.*
import com.zegreatrob.coupling.server.entity.user.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import mu.KotlinLoggingLevel
import mu.LOG_LEVEL
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
    val userRepository: UserRepository
}

private fun repositoryCatalog(jsRepository: dynamic, userCollection: dynamic, user: User) = object : RepositoryCatalog,
        MongoTribeRepository,
        MongoPlayerRepository,
        MongoPairAssignmentDocumentRepository,
        MongoPinRepository,
        MongoUserRepository,
        AuthenticatedUserEmailSyntax {
    override val userCollection: dynamic = userCollection
    override val jsRepository: dynamic = jsRepository
    override val user = user
    override val userRepository = this
    override val tribeRepository = this
    override val playerRepository = this
    override val pairAssignmentDocumentRepository = this
    override val pinRepository = this
}

@Suppress("unused")
@JsName("authActionDispatcher")
fun authActionDispatcher(userCollection: dynamic, userEmail: String): FindOrCreateUserActionDispatcher = object :
        FindOrCreateUserActionDispatcher, FindUserActionDispatcher, MongoUserRepository {
    override val userEmail: String get() = userEmail
    override val userCollection: dynamic = userCollection
    override val userRepository: UserRepository = this

    @JsName("performFindUserAction")
    fun performFindUserAction() = GlobalScope.promise {
        FindUserAction.perform()
                ?.toJson()
    }

    @JsName("performFindOrCreateUserAction")
    fun performFindOrCreateUserAction() = GlobalScope.promise {
        FindOrCreateUserAction.perform()
                .toJson()
    }

    private fun User.toJson() = json(
            "email" to email,
            "tribes" to authorizedTribeIds.map { it.value }.toTypedArray()
    )
}

@Suppress("unused")
@JsName("initializeLogging")
fun initializeLogging(developmentMode: Boolean) {
    LOG_LEVEL = if (developmentMode) {
        KotlinLoggingLevel.DEBUG
    } else {
        KotlinLoggingLevel.INFO
    }
}

@Suppress("unused")
@JsName("commandDispatcher")
fun commandDispatcher(jsRepository: dynamic, userCollection: dynamic, userEmail: String, tribeIds: Array<String>): CommandDispatcher {
    val user = User(userEmail, tribeIds.map(::TribeId).toSet())
    return object : CommandDispatcher,
            RunGameActionDispatcher,
            FindNewPairsActionDispatcher,
            NextPlayerActionDispatcher,
            CreatePairCandidateReportsActionDispatcher,
            CreatePairCandidateReportActionDispatcher,
            Wheel,
            AuthenticatedUserEmailSyntax,
            UserIsAuthorizedActionDispatcher,
            RepositoryCatalog by repositoryCatalog(jsRepository, userCollection, user) {
        override val user = user
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

        @JsName("performUserIsAuthorizedAction")
        fun performUserIsAuthorizedAction(tribeId: String) = GlobalScope.promise {
            UserIsAuthorizedAction(TribeId(tribeId))
                    .perform()
        }
    }
}

private fun userContext(userEmail: String) = object : UserEmailSyntax {
    override val userEmail = userEmail
}
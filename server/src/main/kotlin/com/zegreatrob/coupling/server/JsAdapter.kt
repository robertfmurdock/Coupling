package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.pin.PinRepository
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.player.PlayerRepository
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.TribeRepository
import com.zegreatrob.coupling.model.user.AuthenticatedUserEmailSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserRepository
import com.zegreatrob.coupling.mongo.pairassignments.MongoPairAssignmentDocumentRepository
import com.zegreatrob.coupling.mongo.pin.MongoPinRepository
import com.zegreatrob.coupling.mongo.player.MongoPlayerRepository
import com.zegreatrob.coupling.mongo.tribe.MongoTribeRepository
import com.zegreatrob.coupling.mongo.user.MongoUserRepository
import com.zegreatrob.coupling.server.action.pairassignmentdocument.*
import com.zegreatrob.coupling.server.action.pin.*
import com.zegreatrob.coupling.server.action.player.*
import com.zegreatrob.coupling.server.action.tribe.*
import com.zegreatrob.coupling.server.action.user.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.json

interface CommandDispatcher : ProposeNewPairsCommandDispatcher,
    PlayersQueryDispatcher,
    RetiredPlayersQueryDispatcher,
    SavePlayerCommandDispatcher,
    DeletePlayerCommandDispatcher,
    DeletePinCommandDispatcher,
    PinsQueryDispatcher,
    SavePairAssignmentDocumentCommandDispatcher,
    PairAssignmentDocumentListQueryDispatcher,
    DeletePairAssignmentDocumentCommandDispatcher,
    TribeListQueryDispatcher,
    TribeQueryDispatcher,
    SaveTribeCommandDispatcher,
    DeleteTribeCommandDispatcher {
    override val playerRepository: PlayerRepository
    override val pinRepository: PinRepository
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
    val scope: CoroutineScope = MainScope()

    @JsName("performFindUserAction")
    fun performFindUserAction() = scope.promise {
        FindUserAction.perform()
            ?.toJson()
    }

    @JsName("performFindOrCreateUserAction")
    fun performFindOrCreateUserAction() = scope.promise {
        FindOrCreateUserAction.perform()
            .toJson()
    }

    private fun User.toJson() = json(
        "email" to email,
        "tribes" to authorizedTribeIds.map { it.value }.toTypedArray()
    )
}

@Suppress("unused")
@JsName("commandDispatcher")
fun commandDispatcher(
    jsRepository: dynamic,
    userCollection: dynamic,
    userEmail: String,
    tribeIds: Array<String>
): CommandDispatcher {
    val user = User(userEmail, tribeIds.map(::TribeId).toSet())
    return object : CommandDispatcher,
        RunGameActionDispatcher,
        FindNewPairsActionDispatcher,
        NextPlayerActionDispatcher,
        SavePinCommandDispatcher,
        CreatePairCandidateReportsActionDispatcher,
        CreatePairCandidateReportActionDispatcher,
        Wheel,
        AuthenticatedUserEmailSyntax,
        UserIsAuthorizedActionDispatcher,
        UserIsAuthorizedWithDataActionDispatcher,
        RepositoryCatalog by repositoryCatalog(jsRepository, userCollection, user) {
        override val user = user
        override val actionDispatcher = this
        override val wheel: Wheel = this

        val scope = MainScope()

        @JsName("performTribeListQuery")
        fun performTribeListQuery() = scope.promise {
            TribeListQuery
                .perform()
                .map { it.toJson() }
                .toTypedArray()
        }

        @JsName("performTribeQuery")
        fun performTribeQuery(tribeId: String) = scope.promise {
            TribeQuery(TribeId(tribeId))
                .perform()
                ?.toJson()
        }

        @JsName("performSaveTribeCommand")
        fun performSaveTribeCommand(tribe: Json) = scope.promise {
            SaveTribeCommand(tribe.toTribe())
                .perform()
        }

        @JsName("performDeleteTribeCommand")
        fun performDeleteTribeCommand(tribeId: String) = scope.promise {
            DeleteTribeCommand(TribeId(tribeId))
                .perform()
        }

        @JsName("performSavePlayerCommand")
        fun performSavePlayerCommand(player: Json, tribeId: String) = scope.promise {
            SavePlayerCommand(
                TribeIdPlayer(
                    TribeId(
                        tribeId
                    ), player.toPlayer()
                )
            )
                .perform()
                .toJson()
        }

        @JsName("performSavePinCommand")
        fun performSavePinCommand(pin: Json, tribeId: String) = scope.promise {
            SavePinCommand(
                TribeIdPin(
                    TribeId(
                        tribeId
                    ), pin.toPin()
                )
            )
                .perform()
                .toJson()
        }

        @JsName("performDeletePlayerCommand")
        fun performDeletePlayerCommand(playerId: String) = scope.promise {
            DeletePlayerCommand(TribeId(""), playerId)
                .perform()
        }

        @JsName("performPlayersQuery")
        fun performPlayersQuery(tribeId: String) = scope.promise {
            PlayersQuery(TribeId(tribeId))
                .perform()
                .map { it.toJson() }
                .toTypedArray()
        }

        @JsName("performRetiredPlayersQuery")
        fun performRetiredPlayersQuery(tribeId: String) = scope.promise {
            RetiredPlayersQuery(TribeId(tribeId))
                .perform()
                .map { it.toJson() }
                .toTypedArray()
        }

        @JsName("performPinsQuery")
        fun performPinsQuery(tribeId: String) = scope.promise {
            PinsQuery(TribeId(tribeId))
                .perform()
                .map { it.toJson() }
                .toTypedArray()
        }

        @JsName("performDeletePinCommand")
        fun performDeletePinCommand(pinId: String) = scope.promise {
            DeletePinCommand(pinId)
                .perform()
        }

        @JsName("performProposeNewPairsCommand")
        fun performProposeNewPairsCommand(tribeId: String, players: Array<Json>) = scope.promise {
            ProposeNewPairsCommand(TribeId(tribeId), players.map(Json::toPlayer))
                .perform()
                .toJson()
        }

        @JsName("performSavePairAssignmentDocumentCommand")
        fun performSavePairAssignmentDocumentCommand(tribeId: String, pairAssignmentDocument: Json) =
            scope.promise {
                SavePairAssignmentDocumentCommand(
                    TribeIdPairAssignmentDocument(
                        TribeId(tribeId),
                        pairAssignmentDocument.toPairAssignmentDocument()
                    )
                )
                    .perform()
                    .document
                    .toJson()
            }

        @JsName("performDeletePairAssignmentDocumentCommand")
        fun performDeletePairAssignmentDocumentCommand(id: String) = scope.promise {
            DeletePairAssignmentDocumentCommand(TribeId(""), id.let(::PairAssignmentDocumentId))
                .perform()
        }

        @JsName("performPairAssignmentDocumentListQuery")
        fun performPairAssignmentDocumentListQuery(tribeId: String) = scope.promise {
            PairAssignmentDocumentListQuery(TribeId(tribeId))
                .perform()
                .map { it.toJson() }
                .toTypedArray()
        }

        @JsName("performUserIsAuthorizedAction")
        fun performUserIsAuthorizedAction(tribeId: String) = scope.promise {
            UserIsAuthorizedAction(TribeId(tribeId))
                .perform()
        }

        @JsName("performUserIsAuthorizedWithDataAction")
        fun performUserIsAuthorizedWithDataAction(tribeId: String) = scope.promise {
            UserIsAuthorizedWithDataAction(TribeId(tribeId))
                .perform()
                ?.let { (tribe, players) ->
                    json("tribe" to tribe.toJson(), "players" to players.map { it.toJson() }.toTypedArray())
                }
        }
    }
}

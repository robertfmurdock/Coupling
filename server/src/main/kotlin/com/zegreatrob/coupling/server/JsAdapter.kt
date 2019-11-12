package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.pin.PinRepository
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.player.PlayerRepository
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
import com.zegreatrob.coupling.server.action.user.*
import com.zegreatrob.coupling.server.entity.player.PlayerDispatcherJs
import com.zegreatrob.coupling.server.entity.tribe.TribeDispatcherJs
import kotlinx.coroutines.*
import kotlin.js.Json
import kotlin.js.json

interface CommandDispatcher : ProposeNewPairsCommandDispatcher,
    DeletePinCommandDispatcher,
    PinsQueryDispatcher,
    SavePairAssignmentDocumentCommandDispatcher,
    PairAssignmentDocumentListQueryDispatcher,
    DeletePairAssignmentDocumentCommandDispatcher {
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
    tribeIds: Array<String>,
    path: String
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
        TribeDispatcherJs,
        PlayerDispatcherJs,
        RepositoryCatalog by repositoryCatalog(jsRepository, userCollection, user) {
        override val user = user
        override val actionDispatcher = this
        override val wheel: Wheel = this

        override val scope = MainScope() + CoroutineName(path)

        @JsName("performSavePinCommand")
        fun performSavePinCommand(pin: Json, tribeId: String) = scope.promise {
            SavePinCommand(
                TribeIdPin(TribeId(tribeId), pin.toPin())
            )
                .perform()
                .toJson()
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
            DeletePinCommand(TribeId(""), pinId)
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

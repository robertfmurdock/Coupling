package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.entity.pairassignment.PairAssignmentDispatcherJs
import com.zegreatrob.coupling.server.entity.pin.PinDispatcherJs
import com.zegreatrob.coupling.server.entity.player.PlayerDispatcherJs
import com.zegreatrob.coupling.server.entity.tribe.TribeDispatcherJs
import com.zegreatrob.coupling.server.entity.user.UserDispatcherJs
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.plus

@Suppress("unused")
@JsName("commandDispatcher")
fun commandDispatcher(
    jsRepository: dynamic,
    userCollection: dynamic,
    userEmail: String,
    tribeIds: Array<String>,
    path: String
): Any {
    val user = User(userEmail, tribeIds.map(::TribeId).toSet())
    return CommandDispatcher(user, jsRepository, userCollection, path)
}

class CommandDispatcher(override val user: User, jsRepository: dynamic, userCollection: dynamic, path: String) :
    TribeDispatcherJs,
    PlayerDispatcherJs,
    PinDispatcherJs,
    PairAssignmentDispatcherJs,
    UserDispatcherJs,
    RepositoryCatalog by MongoRepositoryCatalog(userCollection, jsRepository, user) {
    override val scope = MainScope() + CoroutineName(path)
}

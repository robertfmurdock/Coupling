package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeElement
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import kotlin.js.Json
import kotlin.js.json

class LocalStorageRepositoryBackend {
    val tribe by localBackend(Tribe::toJson, Json::toTribe)
    val player by localBackend(
        { it.element.toJson().add(it.tribeIdJson()) },
        { TribeIdPlayer(it.tribeId(), it.toPlayer()) }
    )
    val pairAssignments by localBackend(
        { it.element.toJson().add(it.tribeIdJson()) },
        { TribeIdPairAssignmentDocument(it.tribeId(), it.toPairAssignmentDocument()) })

    val pin by localBackend(
        { it.element.toJson().add(it.tribeIdJson()) },
        { TribeIdPin(it.tribeId(), it.toPin()) }
    )

    val user by localBackend(User::toJson, Json::toUser)

    private fun <T> TribeElement<T>.tribeIdJson(): Json = json("tribeId" to id.value)
    private fun Json.tribeId() = this["tribeId"].toString().let(::TribeId)
}

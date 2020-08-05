package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeElement
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.memory.RecordBackend
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.browser.window
import kotlin.js.Json
import kotlin.js.json
import kotlin.reflect.KProperty

class MemoryRepositoryBackend {
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

fun <T : Any> localBackend(
    toJson: (T) -> Json,
    toEntity: (Json) -> T
) = LocalBackendProperty(toJson, toEntity)

class LocalBackendProperty<T : Any>(val toJson: (T) -> Json, val toEntity: (Json) -> T) {
    operator fun getValue(backend: MemoryRepositoryBackend, property: KProperty<*>) =
        LocalStorageRecordBackend<T>(property.name, toJson, toEntity)

}

class LocalStorageRecordBackend<T>(
    val name: String,
    val toJson: (T) -> Json,
    val toEntity: (Json) -> T
) : RecordBackend<T> {
    override var records: List<Record<T>>
        get() = parseBackend()?.map { it.recordFor(toEntity(it)) }?.toList() ?: emptyList()
        set(value) {
            window.localStorage["$name-backend"] = value.map { toJson(it.data).add(it.toJson()) }.toTypedArray().let(JSON::stringify)
        }

    private fun parseBackend(): Array<Json>? = window.localStorage["$name-backend"]?.let(JSON::parse)

}
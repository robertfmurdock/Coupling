package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.model.Record
import kotlin.js.Json
import kotlin.reflect.KProperty

fun <T : Any> localBackend(toJson: (Record<T>) -> Json, toEntity: (Json) -> Record<T>) = LocalBackendProperty(toJson, toEntity)

class LocalBackendProperty<T : Any>(private val toJson: (Record<T>) -> Json, private val toEntity: (Json) -> Record<T>) {
    operator fun getValue(backend: LocalStorageRepositoryBackend, property: KProperty<*>) =
        LocalStorageRecordBackend(property.name, toJson, toEntity)
}

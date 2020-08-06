package com.zegreatrob.coupling.client

import kotlin.js.Json
import kotlin.reflect.KProperty

fun <T : Any> localBackend(toJson: (T) -> Json, toEntity: (Json) -> T) = LocalBackendProperty(toJson, toEntity)

class LocalBackendProperty<T : Any>(private val toJson: (T) -> Json, private val toEntity: (Json) -> T) {
    operator fun getValue(backend: LocalStorageRepositoryBackend, property: KProperty<*>) =
        LocalStorageRecordBackend(property.name, toJson, toEntity)
}

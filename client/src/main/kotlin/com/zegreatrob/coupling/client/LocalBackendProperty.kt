package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.model.Record
import kotlin.reflect.KProperty

fun <T : Any, S> localBackend(toJson: (Record<T>) -> S, toEntity: (S) -> Record<T>) = LocalBackendProperty(toJson, toEntity)

class LocalBackendProperty<T : Any, S>(private val toJson: (Record<T>) -> S, private val toEntity: (S) -> Record<T>) {
    operator fun getValue(backend: LocalStorageRepositoryBackend, property: KProperty<*>) =
        LocalStorageRecordBackend(property.name, toJson, toEntity)
}

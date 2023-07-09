package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.model.Record
import kotlin.reflect.KProperty

fun <T : Any> localBackend(toJson: (List<Record<T>>) -> String, toEntity: (String) -> List<Record<T>>) =
    LocalBackendProperty(toJson, toEntity)

class LocalBackendProperty<T : Any>(
    private val toJson: (List<Record<T>>) -> String,
    private val toEntity: (String) -> List<Record<T>>,
) {
    operator fun getValue(backend: LocalStorageRepositoryBackend, property: KProperty<*>) =
        LocalStorageRecordBackend(property.name, toJson, toEntity)
}

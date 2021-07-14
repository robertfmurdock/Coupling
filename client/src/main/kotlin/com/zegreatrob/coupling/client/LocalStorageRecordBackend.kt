package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.repository.memory.RecordBackend
import kotlinx.browser.window
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.js.Json

class LocalStorageRecordBackend<T>(
    val name: String,
    val toJson: (Record<T>) -> Json,
    val toEntity: (Json) -> Record<T>
) : RecordBackend<T> {
    override var records: List<Record<T>>
        get() = window.localStorage["$name-backend"].toRecord() ?: emptyList()
        set(value) {
            window.localStorage["$name-backend"] = value.toJsonString()
        }

    private fun String?.toRecord() = parseBackend()?.map { toEntity(it) }?.toList()

    private fun List<Record<T>>.toJsonString() = map { toJson(it) }
        .toTypedArray()
        .let(JSON::stringify)

    private fun String?.parseBackend(): Array<Json>? = this?.let(JSON::parse)

}

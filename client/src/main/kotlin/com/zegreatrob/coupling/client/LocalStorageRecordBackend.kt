package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.json.recordFor
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.repository.memory.RecordBackend
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.browser.window
import kotlin.js.Json

class LocalStorageRecordBackend<T>(
    val name: String,
    val toJson: (T) -> Json,
    val toEntity: (Json) -> T
) : RecordBackend<T> {
    override var records: List<Record<T>>
        get() = window.localStorage["$name-backend"].toRecord() ?: emptyList()
        set(value) {
            window.localStorage["$name-backend"] = value.toJsonString()
        }

    private fun String?.toRecord() = parseBackend()?.map { it.recordFor(toEntity(it)) }?.toList()

    private fun List<Record<T>>.toJsonString() = map { toJson(it.data).add(it.toJson()) }
        .toTypedArray()
        .let(JSON::stringify)

    private fun String?.parseBackend(): Array<Json>? = this?.let(JSON::parse)

}

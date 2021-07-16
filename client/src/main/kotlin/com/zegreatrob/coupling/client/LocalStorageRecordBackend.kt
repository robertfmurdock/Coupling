package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.repository.memory.RecordBackend
import kotlinx.browser.window
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.w3c.dom.get
import org.w3c.dom.set

class LocalStorageRecordBackend<T, S>(
    val name: String,
    val toSerializable: (Record<T>) -> S,
    val toModel: (S) -> Record<T>
) : RecordBackend<T> {
    override var records: List<Record<T>>
        get() = window.localStorage["$name-backend"].toRecord() ?: emptyList()
        set(value) {
            window.localStorage["$name-backend"] = value.toJsonString()
        }

    private fun String?.toRecord() = this?.let { couplingJsonFormat.decodeFromString<List<S>>(this).map(toModel) }

    private fun List<Record<T>>.toJsonString() = couplingJsonFormat.encodeToString(map { toSerializable(it) })

}

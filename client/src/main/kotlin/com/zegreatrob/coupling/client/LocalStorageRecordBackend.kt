package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.json.fromJsonString
import com.zegreatrob.coupling.json.toJsonString
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.repository.memory.RecordBackend
import kotlinx.browser.window
import org.w3c.dom.get
import org.w3c.dom.set

class LocalStorageRecordBackend<T, S>(
    val name: String,
    val toSerializable: (Record<T>) -> S,
    private val toModel: (S) -> Record<T>
) : RecordBackend<T> {
    override var records: List<Record<T>>
        get() = window.localStorage["$name-backend"]?.toRecord() ?: emptyList()
        set(value) {
            window.localStorage["$name-backend"] = value.toJsonString()
        }

    private fun String.toRecord() = fromJsonString<List<S>>().map(toModel)

    private fun List<Record<T>>.toJsonString() = map { toSerializable(it) }.toJsonString()

}

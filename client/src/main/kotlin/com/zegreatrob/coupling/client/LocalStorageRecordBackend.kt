package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.repository.memory.RecordBackend
import kotlinx.browser.window
import org.w3c.dom.get
import org.w3c.dom.set

class LocalStorageRecordBackend<T>(
    val name: String,
    val toSerializableString: (List<Record<T>>) -> String,
    private val toModel: (String) -> List<Record<T>>,
) : RecordBackend<T> {
    override var records: List<Record<T>>
        get() = window.localStorage["$name-backend"]?.let { toModel(it) } ?: emptyList()
        set(value) {
            window.localStorage["$name-backend"] = toSerializableString(value)
        }
}

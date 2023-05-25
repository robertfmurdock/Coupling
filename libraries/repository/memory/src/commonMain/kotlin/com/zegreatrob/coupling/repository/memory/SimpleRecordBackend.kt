package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.Record

class SimpleRecordBackend<T> : RecordBackend<T> {
    override var records: List<Record<T>> = emptyList()
}

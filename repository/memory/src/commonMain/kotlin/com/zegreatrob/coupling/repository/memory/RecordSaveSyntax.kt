package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.Record

interface RecordSaveSyntax<T> {
    var records: List<Record<T>>

    fun Record<T>.save() {
        records = records + this
    }
}
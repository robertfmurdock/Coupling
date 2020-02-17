package com.zegreatrob.coupling.repository.memory

interface RecordSaveSyntax<T> {
    var records: List<Record<T>>

    fun Record<T>.save() {
        records = records + this
    }
}
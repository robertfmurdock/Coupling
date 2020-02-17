package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.DateTime

interface TypeRecordSyntax<T> {

    fun T.record() = Record(
        data = this,
        timestamp = DateTime.now(),
        isDeleted = false
    )

    fun T.deletionRecord() = Record(
        data = this,
        timestamp = DateTime.now(),
        isDeleted = true
    )

}
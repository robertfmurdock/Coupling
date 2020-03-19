package com.zegreatrob.coupling.model

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.tribe.TribeElement
import com.zegreatrob.coupling.model.tribe.TribeId

data class Record<T>(
    val data: T,
    val modifyingUserEmail: String,
    val isDeleted: Boolean = false,
    val timestamp: DateTime = DateTime.now()
)

fun <T> List<Record<T>>.data() = map { it.data }

typealias TribeRecord<T> = Record<TribeElement<T>>

fun <T> tribeRecord(
    tribeId: TribeId,
    data: T,
    modifyingUserEmail: String,
    isDeleted: Boolean = false,
    timestamp: DateTime = DateTime.now()
) = TribeRecord(TribeElement(tribeId, data), modifyingUserEmail, isDeleted, timestamp)

val <T> TribeRecord<T>.element get() = this.data.element

val <T> List<TribeRecord<T>>.elements get() = map { it.element }

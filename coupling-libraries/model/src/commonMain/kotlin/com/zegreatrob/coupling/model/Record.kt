package com.zegreatrob.coupling.model

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.tribe.PartyId
import com.zegreatrob.coupling.model.tribe.PartyElement

data class Record<T>(
    val data: T,
    val modifyingUserId: String,
    val isDeleted: Boolean = false,
    val timestamp: DateTime = DateTime.now()
)

fun <T> List<Record<T>>.data() = map { it.data }

typealias TribeRecord<T> = Record<PartyElement<T>>

fun <T> tribeRecord(
    partyId: PartyId,
    data: T,
    modifyingUserEmail: String,
    isDeleted: Boolean = false,
    timestamp: DateTime = DateTime.now()
) = TribeRecord(PartyElement(partyId, data), modifyingUserEmail, isDeleted, timestamp)

val <T> TribeRecord<T>.element get() = this.data.element

val <T> List<TribeRecord<T>>.elements get() = map { it.element }

package com.zegreatrob.coupling.model

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId

data class Record<T>(
    val data: T,
    val modifyingUserId: String,
    val isDeleted: Boolean = false,
    val timestamp: DateTime = DateTime.now()
)

fun <T> List<Record<T>>.data() = map { it.data }

typealias PartyRecord<T> = Record<PartyElement<T>>

fun <T> partyRecord(
    partyId: PartyId,
    data: T,
    modifyingUserEmail: String,
    isDeleted: Boolean = false,
    timestamp: DateTime = DateTime.now()
) = PartyRecord(PartyElement(partyId, data), modifyingUserEmail, isDeleted, timestamp)

val <T> PartyRecord<T>.element get() = this.data.element

val <T> List<PartyRecord<T>>.elements get() = map { it.element }

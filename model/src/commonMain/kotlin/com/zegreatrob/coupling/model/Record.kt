package com.zegreatrob.coupling.model

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.tribe.TribeElement

data class Record<T>(val data: T, val timestamp: DateTime, val isDeleted: Boolean, val modifyingUserEmail: String)

fun <T> List<Record<T>>.data() = map { it.data }

val <T> Record<TribeElement<T>>.element get() = this.data.element

val <T> List<Record<TribeElement<T>>>.elements get() = map { it.element }
package com.zegreatrob.coupling.model

import com.soywiz.klock.DateTime

data class Record<T>(val data: T, val timestamp: DateTime, val isDeleted: Boolean, val modifyingUserEmail: String)


fun <T> List<Record<T>>.data() = map { it.data }

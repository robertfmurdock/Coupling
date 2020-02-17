package com.zegreatrob.coupling.repository.memory

import com.soywiz.klock.DateTime

data class Record<T>(val data: T, val timestamp: DateTime, val isDeleted: Boolean)
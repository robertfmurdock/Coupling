package com.zegreatrob.coupling.repository.tribe

interface TribeListSyntax : TribeRecordSyntax {
    suspend fun getTribes() = getTribeRecords().map { it.data }
}

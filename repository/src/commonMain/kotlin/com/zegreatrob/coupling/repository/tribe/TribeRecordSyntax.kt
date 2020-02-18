package com.zegreatrob.coupling.repository.tribe

interface TribeRecordSyntax {
    val tribeRepository: TribeListGet
    suspend fun getTribeRecords() = tribeRepository.getTribes()
}
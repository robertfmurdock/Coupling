package com.zegreatrob.coupling.repository.tribe

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdGetSyntax : TribeIdGetRecordSyntax {
    suspend fun TribeId.get() = loadRecord()?.data
}

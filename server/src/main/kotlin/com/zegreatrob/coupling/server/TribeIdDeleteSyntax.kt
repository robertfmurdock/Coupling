package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.TribeDelete

interface TribeIdDeleteSyntax {

    val tribeRepository: TribeDelete

    suspend fun TribeId.delete() = tribeRepository.delete(this)
}
package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.server.entity.tribe.TribeDelete

interface TribeIdDeleteSyntax {

    val tribeRepository: TribeDelete

    suspend fun TribeId.delete() = tribeRepository.delete(this)
}
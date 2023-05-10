package com.zegreatrob.coupling.server.action.boost

import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.boost.BoostQuery
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.AuthenticatedUserSyntax
import com.zegreatrob.coupling.repository.BoostGet

interface ServerBoostQueryDispatcher : BoostGetSyntax, AuthenticatedUserSyntax, BoostQuery.Dispatcher {
    override suspend fun perform(command: BoostQuery) = load()
        ?.successResult()
        ?: NotFoundResult("boost")
}

interface BoostGetSyntax {
    val boostRepository: BoostGet
    suspend fun load(): Record<Boost>? = boostRepository.get()
}

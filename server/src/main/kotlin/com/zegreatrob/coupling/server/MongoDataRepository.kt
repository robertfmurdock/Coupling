package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toPins
import com.zegreatrob.coupling.entity.CouplingDataRepository
import kotlinx.coroutines.asDeferred
import kotlin.js.Json
import kotlin.js.Promise

interface MongoDataRepository : CouplingDataRepository, MongoPairAssignmentDocumentRepository {

    override val jsRepository: dynamic

    override fun getPinsAsync(tribeId: TribeId) = requestPins(tribeId)
            .then { it.toPins() }
            .asDeferred()

    private fun requestPins(tribeId: TribeId) = jsRepository.requestPins(tribeId.value).unsafeCast<Promise<Array<Json>>>()

    private fun requestTribe(tribeId: TribeId) = jsRepository.requestTribe(tribeId.value).unsafeCast<Promise<Json>>()

}


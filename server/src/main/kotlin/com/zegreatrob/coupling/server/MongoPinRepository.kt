package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.common.entity.pin.Pin
import com.zegreatrob.coupling.common.entity.pin.TribeIdPin
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.server.entity.pin.PinRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.js.Json
import kotlin.js.json

interface MongoPinRepository : PinRepository, PinToDbSyntax, DbRecordSaveSyntax, DbRecordLoadSyntax {

    val jsRepository: dynamic
    val pinCollection: dynamic get() = jsRepository.pinCollection

    override fun getPinsAsync(tribeId: TribeId): Deferred<List<Pin>> = GlobalScope.async {
        findByQuery(json("tribe" to tribeId.value), pinCollection)
            .map { it.toDbPin() }
    }

    override suspend fun save(tribeIdPin: TribeIdPin) = tribeIdPin.toDbJson()
        .savePinJson()

    private fun TribeIdPin.toDbJson() = pin.toDbJson()
        .apply { this["tribe"] = tribeId.value }

    private suspend fun Json.savePinJson() = this.save(pinCollection)


}


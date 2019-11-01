package com.zegreatrob.coupling.mongo.pin

import com.zegreatrob.coupling.core.entity.pin.Pin
import com.zegreatrob.coupling.core.entity.pin.TribeIdPin
import com.zegreatrob.coupling.core.entity.tribe.TribeId
import com.zegreatrob.coupling.mongo.DbRecordDeleteSyntax
import com.zegreatrob.coupling.mongo.DbRecordLoadSyntax
import com.zegreatrob.coupling.mongo.DbRecordSaveSyntax
import com.zegreatrob.coupling.server.entity.pin.PinRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.js.Json
import kotlin.js.json

interface MongoPinRepository : PinRepository,
    PinToDbSyntax,
    DbRecordSaveSyntax,
    DbRecordLoadSyntax,
    DbRecordDeleteSyntax {

    val jsRepository: dynamic
    val pinCollection: dynamic get() = jsRepository.pinCollection

    override fun getPinsAsync(tribeId: TribeId): Deferred<List<Pin>> = GlobalScope.async {
        findByQuery(json("tribe" to tribeId.value), pinCollection)
            .map { it.fromDbToPin() }
    }

    override suspend fun save(tribeIdPin: TribeIdPin) = tribeIdPin.toDbJson()
        .savePinJson()

    private fun TribeIdPin.toDbJson() = pin.toDbJson()
        .apply { this["tribe"] = tribeId.value }

    private suspend fun Json.savePinJson() = this.save(pinCollection)

    override suspend fun deletePin(pinId: String) = deleteEntity(
        pinId,
        pinCollection,
        "Pin",
        { toTribeIdPin() },
        { toDbJson() }
    )

    private fun Json.toTribeIdPin() = TribeIdPin(
        tribeId = TribeId(this["tribe"].unsafeCast<String>()),
        pin = applyIdCorrection().fromDbToPin()
    )

}


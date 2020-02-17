package com.zegreatrob.coupling.mongo.pin

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.mongo.DbRecordDeleteSyntax
import com.zegreatrob.coupling.mongo.DbRecordLoadSyntax
import com.zegreatrob.coupling.mongo.DbRecordSaveSyntax
import com.zegreatrob.coupling.mongo.player.JsonRecordSyntax
import com.zegreatrob.coupling.repository.pin.PinRepository
import kotlin.js.Json
import kotlin.js.json

interface MongoPinRepository : PinRepository,
    PinToDbSyntax,
    JsonRecordSyntax,
    DbRecordSaveSyntax,
    DbRecordLoadSyntax,
    DbRecordDeleteSyntax {

    val jsRepository: dynamic
    val pinCollection: dynamic get() = jsRepository.pinCollection

    override suspend fun getPins(tribeId: TribeId): List<Record<TribeIdPin>> =
        findByQuery(json("tribe" to tribeId.value), pinCollection)
            .map {
                val pin = it.fromDbToPin()
                it.toDbRecord(TribeIdPin(tribeId, pin))
            }

    override suspend fun save(tribeIdPin: TribeIdPin) = tribeIdPin.toDbJson()
        .savePinJson()

    private fun TribeIdPin.toDbJson() = pin.toDbJson()
        .apply { this["tribe"] = tribeId.value }

    private suspend fun Json.savePinJson() = this.save(pinCollection)

    override suspend fun deletePin(tribeId: TribeId, pinId: String) = deleteEntity(
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


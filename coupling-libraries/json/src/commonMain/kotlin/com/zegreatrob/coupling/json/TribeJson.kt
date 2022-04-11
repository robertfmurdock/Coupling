package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Party
import com.zegreatrob.coupling.model.tribe.PartyId
import kotlinx.serialization.json.jsonObject

val tribeJsonKeys
    get() = Record(Party(PartyId("")), "")
        .toSerializable()
        .toJsonElement()
        .jsonObject
        .keys

val tribeRecordJsonKeys get() = tribeJsonKeys

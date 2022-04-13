package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.serialization.json.jsonObject

val tribeJsonKeys
    get() = Record(Party(PartyId("")), "")
        .toSerializable()
        .toJsonElement()
        .jsonObject
        .keys

val tribeRecordJsonKeys get() = tribeJsonKeys

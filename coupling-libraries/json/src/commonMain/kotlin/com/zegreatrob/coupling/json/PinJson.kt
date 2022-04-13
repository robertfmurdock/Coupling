package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import kotlinx.serialization.json.jsonObject

val pinJsonKeys
    get() = Pin()
        .toSerializable()
        .toJsonElement()
        .jsonObject.keys

val pinRecordJsonKeys
    get() = PartyRecord(PartyId("").with(Pin()), "")
        .toSerializable()
        .toJsonElement()
        .jsonObject.keys

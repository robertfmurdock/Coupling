package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import kotlinx.serialization.json.jsonObject

val pinJsonKeys
    get() = Pin()
        .toSerializable()
        .toJsonElement()
        .jsonObject.keys

val pinRecordJsonKeys
    get() = TribeRecord(TribeId("").with(Pin()), "")
        .toSerializable()
        .toJsonElement()
        .jsonObject.keys

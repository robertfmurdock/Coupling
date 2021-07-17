package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import kotlin.js.Json

val pinJsonKeys
    get() = Pin()
        .toSerializable()
        .toJsonDynamic()
        .unsafeCast<Json>()
        .getKeys()

val pinRecordJsonKeys
    get() = TribeRecord(TribeId("").with(Pin()), "")
        .toSerializable()
        .toJsonDynamic()
        .unsafeCast<Json>()
        .getKeys()

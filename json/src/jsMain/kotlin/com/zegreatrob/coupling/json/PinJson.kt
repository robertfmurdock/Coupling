package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with

val pinJsonKeys
    get() = Pin()
        .toJson()
        .getKeys()

val pinRecordJsonKeys
    get() = TribeRecord(TribeId("").with(Pin()), "")
        .toJson()
        .getKeys()

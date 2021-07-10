package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.pin.Pin

val pinJsonKeys
    get() = Pin()
        .toJson()
        .getKeys()

val pinRecordJsonKeys
    get() = pinJsonKeys + recordJsonKeys

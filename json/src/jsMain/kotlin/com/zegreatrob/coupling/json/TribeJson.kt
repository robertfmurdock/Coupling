package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.Json

val tribeJsonKeys
    get() = Record(Tribe(TribeId("")), "")
        .toSerializable()
        .toJsonDynamic()
        .unsafeCast<Json>()
        .getKeys()

val tribeRecordJsonKeys get() = tribeJsonKeys

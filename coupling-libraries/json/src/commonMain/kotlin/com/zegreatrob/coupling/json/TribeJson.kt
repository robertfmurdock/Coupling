package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.serialization.json.jsonObject

val tribeJsonKeys
    get() = Record(Tribe(TribeId("")), "")
        .toSerializable()
        .toJsonElement()
        .jsonObject
        .keys

val tribeRecordJsonKeys get() = tribeJsonKeys

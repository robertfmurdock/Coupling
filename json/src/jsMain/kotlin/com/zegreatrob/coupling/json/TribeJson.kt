package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId

val tribeJsonKeys
    get() = Record(Tribe(TribeId("")), "")
        .toJson()
        .getKeys()

val tribeRecordJsonKeys get() = tribeJsonKeys

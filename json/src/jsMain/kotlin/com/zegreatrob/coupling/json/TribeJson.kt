package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId

val tribeJsonKeys
    get() = Tribe(TribeId(""))
        .toJson()
        .getKeys()

val tribeRecordJsonKeys get() = tribeJsonKeys + recordJsonKeys

@file:UseSerializers(TribeIdSerializer::class)
package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.serialization.UseSerializers

interface TribeInput {
    val tribeId: TribeId
}

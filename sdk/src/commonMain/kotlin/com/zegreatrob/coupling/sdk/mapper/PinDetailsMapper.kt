package com.zegreatrob.coupling.sdk.mapper

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.sdk.schema.fragment.PinDetails

fun PinDetails.toDomain() = Pin(id = id, name = name, icon = icon)

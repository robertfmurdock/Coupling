package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.sdk.schema.fragment.PinDetails

fun PinDetails.toModel() = Pin(id = id, name = name, icon = icon)

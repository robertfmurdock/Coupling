package com.zegreatrob.coupling.core.entity.pin

import com.zegreatrob.coupling.core.entity.tribe.TribeId

data class Pin(val _id: String? = null, val name: String? = null, val tribe: String? = null, val icon: String? = null)

fun Pin.with(tribeId: TribeId) = TribeIdPin(tribeId, this)

data class TribeIdPin(val tribeId: TribeId, val pin: Pin)
package com.zegreatrob.coupling.model.player.callsign

data class CallSign(val adjective: String, val noun: String) {
    override fun toString() = "$adjective $noun"
}

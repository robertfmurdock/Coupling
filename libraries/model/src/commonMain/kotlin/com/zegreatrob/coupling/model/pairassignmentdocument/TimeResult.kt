package com.zegreatrob.coupling.model.pairassignmentdocument

sealed class TimeResult
data class TimeResultValue(val time: Int) : TimeResult()
object NeverPaired : TimeResult() {
    override fun toString() = "Never Paired"
}
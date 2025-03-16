package com.zegreatrob.coupling.model.player

import kotools.types.text.NotBlankString
import kotools.types.text.toNotBlankString
import kotlin.uuid.Uuid

data class PlayerId(val value: NotBlankString) {
    companion object {
        fun new() = PlayerId(Uuid.random().toString().toNotBlankString().getOrThrow())
    }
}

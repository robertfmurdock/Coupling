package com.zegreatrob.coupling.model.user

import kotools.types.text.NotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.uuid.Uuid

data class UserId(val value: NotBlankString) {
    companion object {
        @OptIn(ExperimentalKotoolsTypesApi::class)
        fun new() = UserId(NotBlankString.Companion.create(Uuid.Companion.random().toString()))
    }
}

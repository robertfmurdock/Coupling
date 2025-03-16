package com.zegreatrob.coupling.model

import kotools.types.text.NotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.uuid.Uuid

data class ContributionId(val value: NotBlankString) {
    companion object {
        @OptIn(ExperimentalKotoolsTypesApi::class)
        fun new() = ContributionId(NotBlankString.Companion.create(Uuid.Companion.random().toString()))
    }
}

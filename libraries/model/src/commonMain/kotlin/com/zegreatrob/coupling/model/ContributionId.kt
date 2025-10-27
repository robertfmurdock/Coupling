package com.zegreatrob.coupling.model

import kotools.types.text.NotBlankString
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.uuid.Uuid

data class ContributionId(val value: NotBlankString) {
    companion object {
        @OptIn(ExperimentalKotoolsTypesApi::class)
        fun new() = ContributionId(Uuid.random().toString().toNotBlankString().getOrThrow())
    }
}

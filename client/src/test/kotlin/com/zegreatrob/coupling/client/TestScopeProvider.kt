package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.ScopeProvider
import com.zegreatrob.testmints.async.ScopeMint

fun ScopeMint.exerciseScopeProvider() = object : ScopeProvider {
    override fun buildScope() = exerciseScope
}
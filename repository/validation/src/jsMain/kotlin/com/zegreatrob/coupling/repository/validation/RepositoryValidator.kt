package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.invoke

interface RepositoryValidator<R, SC : SharedContext<R>> {

    val repositorySetup: TestTemplate<SC>

    fun <C : Any> repositorySetup(
        contextProvider: suspend (SC) -> C,
        additionalActions: suspend C.() -> Unit = {}
    ) = repositorySetup.invoke(contextProvider = contextProvider, additionalActions = additionalActions)

    fun repositorySetup(additionalActions: suspend SC.() -> Unit = {}) =
        repositorySetup.invoke(additionalActions = additionalActions)

}

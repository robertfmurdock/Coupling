package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.testmints.async.TestTemplate

interface RepositoryValidator<R, out SC : SharedContext<R>> {

    val repositorySetup: TestTemplate<SC>
}

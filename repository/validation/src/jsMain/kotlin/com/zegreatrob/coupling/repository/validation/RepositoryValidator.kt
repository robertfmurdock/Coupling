package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.testmints.async.TestTemplate

interface RepositoryValidator<R, SC : SharedContext<R>> {

    val repositorySetup: TestTemplate<SC>

}

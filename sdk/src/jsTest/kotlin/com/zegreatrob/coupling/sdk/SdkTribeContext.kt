package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.TribeContext
import com.zegreatrob.coupling.stubmodel.stubUser

class SdkTribeContext<T> (
    override val sdk: Sdk,
    override val repository: T,
    override val tribeId: TribeId,
    override val clock: MagicClock
) : TribeContext<T>, Sdk by sdk {
    override val user = stubUser().copy(email = primaryAuthorizedUsername)
}
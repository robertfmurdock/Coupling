package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.components.RequestSpinAction
import com.zegreatrob.coupling.components.RequestSpinActionDispatcher
import com.zegreatrob.coupling.sdk.SdkSyntax

interface SdkRequestSpinActionDispatcher : RequestSpinActionDispatcher, SdkSyntax {
    override suspend fun perform(action: RequestSpinAction) = with(action) { sdk.requestSpin(partyId, players, pins) }
}

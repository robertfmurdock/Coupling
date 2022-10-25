package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.RequestSpinAction
import com.zegreatrob.coupling.action.RequestSpinActionDispatcher
import com.zegreatrob.coupling.sdk.SdkSyntax

interface SdkRequestSpinActionDispatcher : RequestSpinActionDispatcher, SdkSyntax {
    override suspend fun perform(action: RequestSpinAction) = sdk.perform(action)
}

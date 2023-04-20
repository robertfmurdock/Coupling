package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.pairassignmentdocument.RequestSpinAction
import com.zegreatrob.coupling.sdk.SdkSyntax

interface SdkRequestSpinActionDispatcher : RequestSpinAction.Dispatcher, SdkSyntax {
    override suspend fun perform(action: RequestSpinAction) = sdk.perform(action)
}

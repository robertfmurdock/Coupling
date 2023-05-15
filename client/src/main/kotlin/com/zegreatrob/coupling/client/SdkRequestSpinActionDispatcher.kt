package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.pairassignmentdocument.RequestSpinAction
import com.zegreatrob.coupling.sdk.SdkProviderSyntax

interface SdkRequestSpinActionDispatcher : RequestSpinAction.Dispatcher, SdkProviderSyntax {
    override suspend fun perform(action: RequestSpinAction) = sdk.perform(action)
}

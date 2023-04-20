package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.RequestSpinAction
import com.zegreatrob.coupling.action.user.UserQueryDispatcher

interface BarebonesSdk :
    RepositoryCatalog,
    UserQueryDispatcher,
    RequestSpinAction.Dispatcher

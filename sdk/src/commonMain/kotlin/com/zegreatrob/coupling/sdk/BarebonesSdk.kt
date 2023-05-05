package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.RequestSpinAction
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.user.UserQuery

interface BarebonesSdk :
    RepositoryCatalog,
    UserQuery.Dispatcher,
    RequestSpinAction.Dispatcher,
    SavePlayerCommand.Dispatcher,
    ClientSavePlayerCommandDispatcher

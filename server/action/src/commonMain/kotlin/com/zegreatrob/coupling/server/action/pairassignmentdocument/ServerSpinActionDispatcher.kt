package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.testmints.action.ExecutableActionExecuteSyntax

interface ServerSpinActionDispatcher :
    SpinAction.Dispatcher,
    ExecutableActionExecuteSyntax,
    RunGameAction.Dispatcher {

    override suspend fun perform(action: SpinAction): PairAssignmentDocument? =
        execute(RunGameAction(action.players, action.pins, action.history, action.party))
}

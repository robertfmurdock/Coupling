package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.LoggingActionExecuteSyntax
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.RequestSpinAction
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.repository.pin.PinGet
import com.zegreatrob.coupling.repository.player.PlayerListGet
import com.zegreatrob.coupling.repository.player.PlayerListGetDeleted

interface BarebonesSdk :
    RepositoryCatalog,
    UserQuery.Dispatcher,
    RequestSpinAction.Dispatcher,
    SavePairAssignmentsCommand.Dispatcher,
    SavePartyCommand.Dispatcher,
    SavePinCommand.Dispatcher,
    SavePlayerCommand.Dispatcher,
    DeletePairAssignmentsCommand.Dispatcher,
    DeletePartyCommand.Dispatcher,
    DeletePinCommand.Dispatcher,
    DeletePlayerCommand.Dispatcher,
    LoggingActionExecuteSyntax,
    PartyPlayerQueryDispatcher,
    PlayerListGet,
    PinGet,
    PlayerListGetDeleted

package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.DeleteBoostCommand
import com.zegreatrob.coupling.action.LoggingActionExecuteSyntax
import com.zegreatrob.coupling.action.NewPairAssignmentsCommand
import com.zegreatrob.coupling.action.boost.BoostQuery
import com.zegreatrob.coupling.action.boost.SaveBoostCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.RequestSpinAction
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.stats.StatisticsQuery
import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGetCurrent
import com.zegreatrob.coupling.repository.party.PartyGet
import com.zegreatrob.coupling.repository.pin.PinGet
import com.zegreatrob.coupling.repository.player.PlayerListGet
import com.zegreatrob.coupling.repository.player.PlayerListGetDeleted

interface SdkApi :
    BoostQuery.Dispatcher,
    DeleteBoostCommand.Dispatcher,
    DeletePairAssignmentsCommand.Dispatcher,
    DeletePartyCommand.Dispatcher,
    DeletePinCommand.Dispatcher,
    DeletePlayerCommand.Dispatcher,
    GraphQuery.Dispatcher,
    HistoryQuery.Dispatcher,
    LoggingActionExecuteSyntax,
    NewPairAssignmentsCommand.Dispatcher,
    PairAssignmentDocumentGet,
    PairAssignmentDocumentGetCurrent,
    PartyCurrentDataQuery.Dispatcher,
    PartyGet,
    PartyPinListQuery.Dispatcher,
    PartyPinQuery.Dispatcher,
    PartyPlayerQuery.Dispatcher,
    PartyQuery.Dispatcher,
    PinGet,
    PlayerListGet,
    PlayerListGetDeleted,
    RequestSpinAction.Dispatcher,
    RetiredPlayerListQuery.Dispatcher,
    RetiredPlayerQuery.Dispatcher,
    SaveBoostCommand.Dispatcher,
    SavePairAssignmentsCommand.Dispatcher,
    SavePartyCommand.Dispatcher,
    SavePinCommand.Dispatcher,
    SavePlayerCommand.Dispatcher,
    StatisticsQuery.Dispatcher,
    UserQuery.Dispatcher

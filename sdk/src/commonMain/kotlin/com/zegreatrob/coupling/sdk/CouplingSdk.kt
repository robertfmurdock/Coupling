package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.boost.BoostQuery
import com.zegreatrob.coupling.action.boost.DeleteBoostCommand
import com.zegreatrob.coupling.action.boost.SaveBoostCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.SpinCommand
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.secret.CreateSecretCommand
import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.sdk.gql.GraphQuery

interface CouplingSdk :
    BoostQuery.Dispatcher,
    CreateSecretCommand.Dispatcher,
    DeleteSecretCommand.Dispatcher,
    DeleteBoostCommand.Dispatcher,
    DeletePairAssignmentsCommand.Dispatcher,
    DeletePartyCommand.Dispatcher,
    DeletePinCommand.Dispatcher,
    DeletePlayerCommand.Dispatcher,
    GraphQuery.Dispatcher,
    SpinCommand.Dispatcher,
    SaveBoostCommand.Dispatcher,
    SavePairAssignmentsCommand.Dispatcher,
    SavePartyCommand.Dispatcher,
    SavePinCommand.Dispatcher,
    SavePlayerCommand.Dispatcher

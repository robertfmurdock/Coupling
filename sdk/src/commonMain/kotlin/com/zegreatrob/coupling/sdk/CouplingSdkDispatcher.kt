package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.ApplyBoostCommand
import com.zegreatrob.coupling.action.GrantDiscordAccessCommand
import com.zegreatrob.coupling.action.GrantSlackAccessCommand
import com.zegreatrob.coupling.action.SpinCommand
import com.zegreatrob.coupling.action.boost.DeleteBoostCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.party.ClearContributionsCommand
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.SaveSlackIntegrationCommand
import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.secret.CreateSecretCommand
import com.zegreatrob.coupling.action.secret.DeleteSecretCommand
import com.zegreatrob.coupling.action.user.ConnectUserCommand
import com.zegreatrob.coupling.action.user.CreateConnectUserSecretCommand
import com.zegreatrob.coupling.action.user.DisconnectUserCommand
import com.zegreatrob.coupling.sdk.gql.GqlQuery

interface CouplingSdkDispatcher :
    ApplyBoostCommand.Dispatcher,
    ClearContributionsCommand.Dispatcher,
    CreateConnectUserSecretCommand.Dispatcher,
    CreateSecretCommand.Dispatcher,
    ConnectUserCommand.Dispatcher,
    DeleteBoostCommand.Dispatcher,
    DeletePairAssignmentsCommand.Dispatcher,
    DeletePartyCommand.Dispatcher,
    DeletePinCommand.Dispatcher,
    DeletePlayerCommand.Dispatcher,
    DisconnectUserCommand.Dispatcher,
    DeleteSecretCommand.Dispatcher,
    GrantDiscordAccessCommand.Dispatcher,
    GrantSlackAccessCommand.Dispatcher,
    GqlQuery.Dispatcher,
    SaveContributionCommand.Dispatcher,
    SavePairAssignmentsCommand.Dispatcher,
    SavePartyCommand.Dispatcher,
    SavePinCommand.Dispatcher,
    SavePlayerCommand.Dispatcher,
    SaveSlackIntegrationCommand.Dispatcher,
    SpinCommand.Dispatcher

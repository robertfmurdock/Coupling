package com.zegreatrob.coupling.server.entity.contribution

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.ClearContributionsCommand
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.perform
import com.zegreatrob.coupling.json.ClearContributionsInput
import com.zegreatrob.coupling.json.SaveContributionInput
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val saveContributionResolver = dispatch<JsonNull, SaveContributionInput, CommandDispatcher, SaveContributionCommand, VoidResult, Boolean>(
    dispatcherFunc = DispatcherProviders.command(),
    commandFunc = { _: JsonNull, args: SaveContributionInput -> args.toCommand() },
    fireFunc = ::perform,
    toSerializable = { true },
)

private fun SaveContributionInput.toCommand() = SaveContributionCommand(
    partyId = PartyId(partyId),
    contributionId = contributionId,
    participantEmails = participantEmails,
    hash = hash,
    dateTime = dateTime,
    ease = ease,
    story = story,
    link = link,
    semver = semver,
    label = label,
    firstCommit = firstCommit,
)

val clearContributionsResolver = dispatch<JsonNull, ClearContributionsInput, CommandDispatcher, ClearContributionsCommand, VoidResult, Boolean>(
    dispatcherFunc = DispatcherProviders.command(),
    commandFunc = { _: JsonNull, args: ClearContributionsInput -> args.toCommand() },
    fireFunc = ::perform,
    toSerializable = { true },
)

private fun ClearContributionsInput.toCommand() = ClearContributionsCommand(
    partyId = PartyId(partyId),
)

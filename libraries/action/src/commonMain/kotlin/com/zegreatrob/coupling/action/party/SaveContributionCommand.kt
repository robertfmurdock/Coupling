package com.zegreatrob.coupling.action.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotlinx.datetime.Instant

@ActionMint
data class SaveContributionCommand(
    val partyId: PartyId,
    val contributionId: String,
    val participantEmails: Set<String>,
    val hash: String? = null,
    val dateTime: Instant? = null,
    val ease: Int? = null,
    val story: String? = null,
    val link: String? = null,
    val semver: String? = null,
    val label: String? = null,
    val firstCommit: String? = null,
) {
    fun interface Dispatcher {
        suspend fun perform(command: SaveContributionCommand): VoidResult
    }
}

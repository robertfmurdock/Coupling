package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.matches
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotlin.time.Clock
import kotlin.time.Duration

@ActionMint
data class PairContributionQuery(
    val partyId: PartyId,
    val pair: CouplingPair,
    val window: Duration? = null,
    val limit: Int? = null,
) {
    interface Dispatcher : PartyIdContributionsTrait {
        suspend fun perform(query: PairContributionQuery): ContributionReport = query.partyId.contributions(query.window, query.limit)
            .filter(byTargetPair(query.pair.targetPlayerEmailGroups()))
            .filter(byWindow(query))
            .let { contributionReport(it, query.partyId) }

        private fun byWindow(query: PairContributionQuery): (PartyRecord<Contribution>) -> Boolean {
            val window = query.window ?: return { true }
            val windowStart = Clock.System.now() - window
            return { it.data.element.dateTime?.let { dateTime -> windowStart < dateTime } ?: false }
        }

        private fun byTargetPair(targetPlayerEmailGroups: Array<Player>) = { record: PartyRecord<Contribution> ->
            pairMatches(
                record.data.element.participantEmails,
                targetPlayerEmailGroups,
            )
        }
    }
}

private fun CouplingPair.targetPlayerEmailGroups() = asArray()

private fun pairMatches(pairEmails: Set<String>, targetPlayerEmailGroups: Array<Player>): Boolean = allGroupsAreMatched(targetPlayerEmailGroups, pairEmails) &&
    allEmailsAreMatches(pairEmails, targetPlayerEmailGroups)

private fun allEmailsAreMatches(
    pairEmails: Set<String>,
    targetPlayerEmailGroups: Array<Player>,
) = pairEmails.all { email -> targetPlayerEmailGroups.any { player -> player.matches(email) } }

private fun allGroupsAreMatched(
    targetPlayerEmailGroups: Array<Player>,
    pairEmails: Set<String>,
) = targetPlayerEmailGroups.all { player ->
    pairEmails.any { email -> player.matches(email) }
}

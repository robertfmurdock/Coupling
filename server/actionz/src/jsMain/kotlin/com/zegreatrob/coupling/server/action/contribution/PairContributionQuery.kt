package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PairContributionQuery(val partyId: PartyId, val pair: CouplingPair) {
    interface Dispatcher : PartyIdContributionsSyntax {
        suspend fun perform(query: PairContributionQuery) = query.partyId.contributions()
            .filter(byTargetPair(query.pair.targetPlayerEmailGroups()))

        private fun byTargetPair(targetPlayerEmailGroups: List<Set<String>>) = { record: PartyRecord<Contribution> ->
            pairMatches(
                record.data.element.participantEmails,
                targetPlayerEmailGroups,
            )
        }
    }
}

private fun CouplingPair.targetPlayerEmailGroups() = asArray()
    .map { (listOf(it.email) + it.additionalEmails).filter(String::isNotEmpty).toSet() }

private fun pairMatches(pairEmails: Set<String>, targetPlayerEmailGroups: List<Set<String>>): Boolean =
    allGroupsAreMatched(targetPlayerEmailGroups, pairEmails) &&
        allEmailsAreMatches(pairEmails, targetPlayerEmailGroups)

private fun allEmailsAreMatches(
    pairEmails: Set<String>,
    targetPlayerEmailGroups: List<Set<String>>,
) = pairEmails.all { email -> targetPlayerEmailGroups.any { group -> email in group } }

private fun allGroupsAreMatched(
    targetPlayerEmailGroups: List<Set<String>>,
    pairEmails: Set<String>,
) = targetPlayerEmailGroups.all { group ->
    group.any(pairEmails::contains)
}

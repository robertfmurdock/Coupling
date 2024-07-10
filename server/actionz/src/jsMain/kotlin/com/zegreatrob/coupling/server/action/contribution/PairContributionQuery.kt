package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.contribution.ContributionGet
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PairContributionQuery(val partyId: PartyId, val pair: CouplingPair) {
    interface Dispatcher {
        val contributionRepository: ContributionGet
        suspend fun perform(query: PairContributionQuery): List<PartyRecord<Contribution>> {
            val targetPlayerEmailGroups = query.pair.targetPlayerEmailGroups()
            return query.partyId.partyContributions()
                .filter { pairMatches(it.data.element.participantEmails, targetPlayerEmailGroups) }
        }

        suspend fun PartyId.partyContributions() = contributionRepository.get(this)
    }
}

private fun CouplingPair.targetPlayerEmailGroups(): Set<Set<String>> = asArray()
    .map { (listOf(it.email) + it.additionalEmails).filter(String::isNotEmpty).toSet() }
    .toSet()

private fun pairMatches(pairEmails: Set<String>, targetPlayerEmailGroups: Set<Set<String>>): Boolean =
    allGroupsAreMatched(targetPlayerEmailGroups, pairEmails) &&
        allEmailsAreMatches(pairEmails, targetPlayerEmailGroups)

private fun allEmailsAreMatches(
    pairEmails: Set<String>,
    targetPlayerEmailGroups: Set<Set<String>>,
) = pairEmails.all { email -> targetPlayerEmailGroups.any { group -> email in group } }

private fun allGroupsAreMatched(
    targetPlayerEmailGroups: Set<Set<String>>,
    pairEmails: Set<String>,
) = targetPlayerEmailGroups.all { group ->
    group.any(pairEmails::contains)
}

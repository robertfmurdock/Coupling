package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.Contributor
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.contribution.ContributionGet
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PartyContributionQuery(val partyId: PartyId) {
    interface Dispatcher {
        val contributionRepository: ContributionGet
        suspend fun perform(query: PartyContributionQuery): List<PartyRecord<Contribution>> =
            contributionRepository.get(query.partyId)
    }
}

@ActionMint
data class PartyContributorQuery(val partyId: PartyId) {
    interface Dispatcher {
        val contributionRepository: ContributionGet
        suspend fun perform(query: PartyContributorQuery) = contributionRepository.get(query.partyId)
            .elements
            .flatMap { it.participantEmails }
            .toSet()
            .sorted()
            .map { query.partyId.with(Contributor(email = it)) }
    }
}

@ActionMint
data class PairContributionQuery(val partyId: PartyId, val pair: CouplingPair) {
    interface Dispatcher {
        val contributionRepository: ContributionGet
        suspend fun perform(query: PairContributionQuery): List<PartyRecord<Contribution>> {
            val targetEmails = query.pair.asArray().mapNotNull { it.email.ifEmpty { null } }
            return contributionRepository.get(query.partyId)
                .filter { it.data.element.participantEmails.any { email -> targetEmails.contains(email) } }
        }
    }
}

package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.party.PartyId
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

package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserIdSyntax
import com.zegreatrob.coupling.repository.LiveInfoRepository
import com.zegreatrob.coupling.repository.memory.MemoryLiveInfoRepository
import com.zegreatrob.coupling.repository.memory.MemoryPairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.memory.MemoryPartyRepository
import com.zegreatrob.coupling.repository.memory.MemoryPinRepository
import com.zegreatrob.coupling.repository.memory.MemoryPlayerRepository
import com.zegreatrob.coupling.repository.memory.MemorySecretRepository
import com.zegreatrob.coupling.repository.memory.MemoryUserRepository
import com.zegreatrob.coupling.repository.memory.SimpleRecordBackend
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.repository.secret.SecretSave
import com.zegreatrob.coupling.repository.user.UserRepository
import korlibs.time.TimeProvider

class MemoryRepositoryCatalog private constructor(
    override val userId: String,
    override val clock: TimeProvider,
    override val partyRepository: PartyRepository,
    override val playerRepository: PlayerEmailRepository,
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository,
    override val pinRepository: PinRepository,
    override val userRepository: UserRepository,
    override val liveInfoRepository: LiveInfoRepository,
    override val secretRepository: SecretSave,
) :
    RepositoryCatalog,
    UserIdSyntax,
    ClockSyntax {

    init {
        println("initializing MemoryRepositoryCatalog")
    }

    companion object {
        operator fun invoke(userEmail: String, backend: MemoryRepositoryBackend, clock: TimeProvider) =
            MemoryRepositoryCatalog(
                userEmail,
                clock,
                MemoryPartyRepository(userEmail, clock, backend.party),
                MemoryPlayerRepository(userEmail, clock, backend.player),
                MemoryPairAssignmentDocumentRepository(userEmail, clock, backend.pairAssignments),
                MemoryPinRepository(userEmail, clock, backend.pin),
                MemoryUserRepository(userEmail, clock, backend.user),
                MemoryLiveInfoRepository(),
                MemorySecretRepository(),
            )
    }
}

class MemoryRepositoryBackend {
    val party = SimpleRecordBackend<Party>()
    val player = SimpleRecordBackend<PartyElement<Player>>()
    val pairAssignments = SimpleRecordBackend<PartyElement<PairAssignmentDocument>>()
    val pin = SimpleRecordBackend<PartyElement<Pin>>()
    val user = SimpleRecordBackend<User>()
}

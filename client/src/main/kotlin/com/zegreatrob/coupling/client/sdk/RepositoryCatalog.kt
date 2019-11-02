package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.AxiosGetEntitySyntax
import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.client.external.axios.getList
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.json.toPins
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentGetter
import com.zegreatrob.coupling.model.pin.PinGetter
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerGetter
import com.zegreatrob.coupling.model.tribe.TribeGet
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.TribeListGet
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface RepositoryCatalog {
    val tribeRepository: AxiosTribeRepository
    val playerRepository: AxiosPlayerRepository
    val pinRepository: AxiosPinRepository
    val pairAssignmentDocumentRepository: AxiosPairAssignmentsRepository
}

interface AxiosTribeListGet : TribeListGet {
    override fun getTribesAsync() = axios.getList("/api/tribes")
        .then { it.map(Json::toTribe) }
        .asDeferred()
}

interface AxiosGetTribe : AxiosGetEntitySyntax, TribeGet {
    override fun getTribeAsync(tribeId: TribeId) = axios.getEntityAsync("/api/tribes/${tribeId.value}")
        .then(Json::toTribe)
        .asDeferred()
}

interface AxiosTribeRepository : AxiosGetTribe, AxiosTribeListGet
interface AxiosPlayerRepository : AxiosPlayerGetter
interface AxiosPinRepository : AxiosPinGetter
interface AxiosPairAssignmentsRepository : AxiosPairAssignmentDocumentGetter

interface AxiosPlayerGetter : PlayerGetter {
    override fun getPlayersAsync(tribeId: TribeId): Deferred<List<Player>> =
        axios.getList("/api/${tribeId.value}/players")
            .then { it.map(Json::toPlayer) }
            .asDeferred()
}

object AxiosRepositoryCatalog : RepositoryCatalog, AxiosTribeRepository, AxiosPlayerRepository,
    AxiosPairAssignmentsRepository, AxiosPinRepository {
    override val pinRepository get() = this
    override val pairAssignmentDocumentRepository get() = this
    override val playerRepository get() = this
    override val tribeRepository get() = this
}

interface AxiosPairAssignmentDocumentGetter : PairAssignmentDocumentGetter {
    override fun getPairAssignmentsAsync(tribeId: TribeId) = axios.getList("/api/${tribeId.value}/history")
        .then { it.map(Json::toPairAssignmentDocument) }
        .asDeferred()
}

interface AxiosPinGetter : PinGetter {
    override fun getPinsAsync(tribeId: TribeId) = axios.getList("/api/${tribeId.value}/pins")
        .then { it.toPins() }
        .asDeferred()
}
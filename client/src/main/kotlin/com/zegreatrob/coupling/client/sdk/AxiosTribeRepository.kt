package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.AxiosGetEntitySyntax
import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.client.external.axios.getList
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerGetter
import com.zegreatrob.coupling.model.tribe.TribeGet
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface AxiosGetTribe : AxiosGetEntitySyntax, TribeGet {
    override fun getTribeAsync(tribeId: TribeId) = axios.getEntityAsync("/api/tribes/${tribeId.value}")
        .then(Json::toTribe)
        .asDeferred()
}

interface AxiosTribeRepository : AxiosGetTribe


interface RepositoryCatalog {
    val tribeRepository: AxiosTribeRepository
    val playerRepository: AxiosPlayerRepository
}

interface AxiosPlayerRepository : AxiosPlayerGetter

interface AxiosPlayerGetter : PlayerGetter {

    override fun getPlayersAsync(tribeId: TribeId): Deferred<List<Player>> =
        axios.getList("/api/${tribeId.value}/players")
            .then { it.map(Json::toPlayer) }
            .asDeferred()
}

object AxiosRepositoryCatalog : RepositoryCatalog, AxiosTribeRepository, AxiosPlayerRepository {
    override val playerRepository: AxiosPlayerRepository get() = this
    override val tribeRepository: AxiosTribeRepository get() = this
}
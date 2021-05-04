package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.Paths.currentPairsPage
import com.zegreatrob.coupling.client.Paths.newPairAssignmentsPath
import com.zegreatrob.coupling.client.Paths.pinListPath
import com.zegreatrob.coupling.client.Paths.playerConfigPage
import com.zegreatrob.coupling.client.Paths.tribeConfigPath
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with

typealias PathSetter = (String) -> Unit

fun PathSetter.tribeList() = this(Paths.tribeList())

fun PathSetter.pinList(tribeId: TribeId) = this(tribeId.pinListPath())

fun PathSetter.playerConfig(tribeId: TribeId, player: Player) = this(tribeId.with(player).playerConfigPage())

fun PathSetter.currentPairs(tribeId: TribeId) = this(tribeId.currentPairsPage())

fun PathSetter.tribeConfig(tribe: Tribe) = this(tribe.tribeConfigPath())

fun PathSetter.newPairAssignments(tribe: Tribe) = this(newPairAssignmentsPath(tribe))

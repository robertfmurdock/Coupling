package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.Paths.newPairAssignmentsPath
import com.zegreatrob.coupling.client.Paths.pinListPath
import com.zegreatrob.coupling.client.Paths.tribeConfigPath
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId

typealias PathSetter = (String) -> Unit

fun PathSetter.tribeList() = this(Paths.tribeList())

fun PathSetter.pinList(tribeId: TribeId) = this(tribeId.pinListPath())

fun PathSetter.tribeConfig(tribe: Tribe) = this(tribe.tribeConfigPath())

fun PathSetter.newPairAssignments(tribe: Tribe) = this(newPairAssignmentsPath(tribe))

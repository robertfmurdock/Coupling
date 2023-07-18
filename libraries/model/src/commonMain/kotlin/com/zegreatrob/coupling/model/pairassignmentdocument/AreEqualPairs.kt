package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.player.Player

fun areEqualPairs(pair1: CouplingPair, pair2: CouplingPair) =
    areEqualPairArrays(pair1.asArray(), pair2.asArray())

private fun areEqualPairArrays(pair1Array: Array<Player>, pair2Array: Array<Player>) =
    fullyEqualPlayers(pair1Array, pair2Array) ||
        equalPlayerIds(pair1Array, pair2Array)

private fun equalPlayerIds(pair1Array: Array<Player>, pair2Array: Array<Player>) =
    pair1Array.map { it.id }.toSet() == pair2Array.map { it.id }.toSet()

private fun fullyEqualPlayers(pair1Array: Array<Player>, pair2Array: Array<Player>) =
    pair1Array.toSet() == pair2Array.toSet()

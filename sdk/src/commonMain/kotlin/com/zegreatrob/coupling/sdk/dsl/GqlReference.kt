package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.JsonPairAssignmentDocumentRecord
import com.zegreatrob.coupling.json.JsonPartyRecord
import com.zegreatrob.coupling.json.JsonPinData
import com.zegreatrob.coupling.json.JsonPinRecord
import com.zegreatrob.coupling.json.JsonPinnedCouplingPair
import com.zegreatrob.coupling.json.JsonPinnedPlayer
import com.zegreatrob.coupling.json.JsonPlayerRecord
import com.zegreatrob.coupling.json.JsonUser
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.AvatarType
import korlibs.time.DateTime

object GqlReference {
    val user = JsonUser("", "", emptySet())

    private val pinData = JsonPinData(
        id = "",
        name = "",
        icon = "",
    )
    private val pinnedPlayer = JsonPinnedPlayer(
        id = "",
        name = "",
        email = "",
        badge = "",
        callSignAdjective = "",
        callSignNoun = "",
        imageURL = "",
        avatarType = AvatarType.BoringBeam,
        pins = listOf(pinData),
    )
    private val pinnedCouplingPair = JsonPinnedCouplingPair(
        players = listOf(pinnedPlayer),
        pins = setOf(pinData),
    )
    val pairAssignmentRecord = JsonPairAssignmentDocumentRecord(
        id = "",
        date = DateTime.EPOCH,
        pairs = listOf(pinnedCouplingPair),
        partyId = PartyId(""),
        modifyingUserEmail = "",
        isDeleted = false,
        timestamp = DateTime.EPOCH,
    )
    val partyRecord = JsonPartyRecord(
        id = PartyId(""),
        pairingRule = 0,
        badgesEnabled = false,
        defaultBadgeName = "",
        alternateBadgeName = "",
        email = "",
        name = "",
        callSignsEnabled = false,
        animationsEnabled = false,
        animationSpeed = 0.0,
        modifyingUserEmail = "",
        isDeleted = false,
        timestamp = DateTime.EPOCH,
    )

    val pinRecord = JsonPinRecord(
        id = "",
        name = "",
        icon = "",
        partyId = PartyId(""),
        modifyingUserEmail = "",
        isDeleted = false,
        timestamp = DateTime.EPOCH,
    )

    val playerRecord = JsonPlayerRecord(
        id = "",
        name = "",
        email = "",
        badge = "",
        callSignAdjective = "",
        callSignNoun = "",
        imageURL = "",
        avatarType = "",
        partyId = PartyId(""),
        modifyingUserEmail = "",
        isDeleted = false,
        timestamp = DateTime.EPOCH,
    )
}
package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.partyRecordJsonKeys
import com.zegreatrob.coupling.json.pinJsonKeys
import com.zegreatrob.coupling.json.pinRecordJsonKeys
import com.zegreatrob.coupling.json.playerJsonKeys
import com.zegreatrob.coupling.json.playerRecordJsonKeys

enum class PartyGQLComponent(val value: String, val jsonPath: String) {
    PartyData(queryAllFields("party", partyRecordJsonKeys), "/partyData/party"),
    PlayerList(queryAllFields("playerList", playerRecordJsonKeys), "/partyData/playerList"),
    RetiredPlayerList(queryAllFields("retiredPlayers", playerRecordJsonKeys), "/partyData/retiredPlayers"),
    PinList(queryAllFields("pinList", pinRecordJsonKeys), "/partyData/pinList"),
    PairAssignmentDocumentList(
        "pairAssignmentDocumentList {id,date,partyId,isDeleted,modifyingUserEmail,timestamp," +
            "pairs { " +
            "players {" +
            "${playerJsonKeys.joinToString(",")}, " +
            "pins {${pinJsonKeys.joinToString(",")}}" +
            "}" +
            "pins {${pinJsonKeys.joinToString(",")}}" +
            "}" +
            "}",
        "/partyData/pairAssignmentDocumentList"
    ),
    CurrentPairAssignmentDocument(
        "currentPairAssignmentDocument {id,date,partyId,isDeleted,modifyingUserEmail,timestamp," +
            "pairs { " +
            "players {" +
            "${playerJsonKeys.joinToString(",")}, " +
            "pins {${pinJsonKeys.joinToString(",")}}" +
            "}" +
            "pins {${pinJsonKeys.joinToString(",")}}" +
            "}" +
            "}",
        "/partyData/currentPairAssignmentDocument"
    );
}

private fun queryAllFields(name: String, keys: Set<String>) = "$name {${keys.joinToString(",")}}"

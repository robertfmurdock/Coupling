package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.pinJsonKeys
import com.zegreatrob.coupling.json.pinRecordJsonKeys
import com.zegreatrob.coupling.json.playerJsonKeys
import com.zegreatrob.coupling.json.playerRecordJsonKeys
import com.zegreatrob.coupling.json.tribeRecordJsonKeys

enum class TribeGQLComponent(val value: String, val jsonPath: String) {
    TribeData(queryAllFields("tribe", tribeRecordJsonKeys), "/tribeData/tribe"),
    PlayerList(queryAllFields("playerList", playerRecordJsonKeys), "/tribeData/playerList"),
    RetiredPlayerList(queryAllFields("retiredPlayers", playerRecordJsonKeys), "/tribeData/retiredPlayers"),
    PinList(queryAllFields("pinList", pinRecordJsonKeys), "/tribeData/pinList"),
    PairAssignmentDocumentList(
        "pairAssignmentDocumentList {id,date,tribeId,isDeleted,modifyingUserEmail,timestamp," +
            "pairs { " +
            "players {" +
            "${playerJsonKeys.joinToString(",")}, " +
            "pins {${pinJsonKeys.joinToString(",")}}" +
            "}" +
            "pins {${pinJsonKeys.joinToString(",")}}" +
            "}" +
            "}",
        "/tribeData/pairAssignmentDocumentList"
    ),
    CurrentPairAssignmentDocument(
        "currentPairAssignmentDocument {id,date,tribeId,isDeleted,modifyingUserEmail,timestamp," +
            "pairs { " +
            "players {" +
            "${playerJsonKeys.joinToString(",")}, " +
            "pins {${pinJsonKeys.joinToString(",")}}" +
            "}" +
            "pins {${pinJsonKeys.joinToString(",")}}" +
            "}" +
            "}",
        "/tribeData/currentPairAssignmentDocument"
    );
}

private fun queryAllFields(name: String, keys: Set<String>) = "$name {${keys.joinToString(",")}}"

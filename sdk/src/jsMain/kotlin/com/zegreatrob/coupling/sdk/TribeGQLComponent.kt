package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.*

enum class TribeGQLComponent(val value: String, val jsonPath: String) {
    TribeData(queryAllFields("tribe", tribeRecordJsonKeys), "/tribeData/tribe"),
    PlayerList(queryAllFields("playerList", playerRecordJsonKeys), "/tribeData/playerList"),
    RetiredPlayerList(queryAllFields("retiredPlayers", playerRecordJsonKeys), "/tribeData/retiredPlayers"),
    PinList(queryAllFields("pinList", pinRecordJsonKeys), "/tribeData/pinList"),
    PairAssignmentDocumentList(
        "pairAssignmentDocumentList {_id,date,modifyingUserEmail,timestamp," +
                "pairs { " +
                "players {" +
                "${playerJsonKeys.joinToString(",")}, " +
                "pins {${pinJsonKeys.joinToString(",")}}" +
                "}" +
                "pins {${pinJsonKeys.joinToString(",")}}" +
                "}" +
                "}", "/tribeData/pairAssignmentDocumentList"
    );
}

private fun queryAllFields(name: String, keys: Array<String>) = "$name {${keys.joinToString(",")}}"
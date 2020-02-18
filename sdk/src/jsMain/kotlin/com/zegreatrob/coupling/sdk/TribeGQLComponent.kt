package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.*

enum class TribeGQLComponent(val value: String, val jsonPath: String) {
    TribeData(tribeJsonKeys.joinToString(","), "/tribe"),
    PlayerList("playerList {${playerRecordJsonKeys.joinToString(",")}}", "/tribe/playerList"),
    PinList("pinList {${pinRecordJsonKeys.joinToString(",")}}", "/tribe/pinList"),
    PairAssignmentDocumentList(
        "pairAssignmentDocumentList {_id,date,modifyingUserEmail,timestamp," +
                "pairs { " +
                "players {" +
                "${playerJsonKeys.joinToString(",")}, " +
                "pins {${pinJsonKeys.joinToString(",")}}" +
                "}" +
                "pins {${pinJsonKeys.joinToString(",")}}" +
                "}" +
                "}", "/tribe/pairAssignmentDocumentList"
    );


}
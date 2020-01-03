package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.pinJsonKeys
import com.zegreatrob.coupling.json.playerJsonKeys
import com.zegreatrob.coupling.json.tribeJsonKeys

enum class TribeGQLComponent(val value: String, val jsonPath: String) {
    TribeData(tribeJsonKeys.joinToString(","), "/tribe"),
    PlayerList("playerList {${playerJsonKeys.joinToString(",")}}", "/tribe/playerList"),
    PinList("pinList {${pinJsonKeys.joinToString(",")}}", "/tribe/pinList"),
    PairAssignmentDocumentList(
        "pairAssignmentDocumentList {_id,date," +
                "pairs { ${playerJsonKeys.joinToString(",")}, " +
                "pins {${pinJsonKeys.joinToString(",")}}" +
                "}" +
                "}", "/tribe/pairAssignmentDocumentList"
    );


}
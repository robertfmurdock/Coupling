package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.pairassignmentdocument.SavePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.action.pairassignmentdocument.SavePairAssignmentDocumentCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import kotlinx.coroutines.promise
import kotlin.js.Json

interface SavePairAssignmentDocumentCommandDispatcherJs : SavePairAssignmentDocumentCommandDispatcher, ScopeSyntax {
    @JsName("performSavePairAssignmentDocumentCommand")
    fun performSavePairAssignmentDocumentCommand(tribeId: String, pairAssignmentDocument: Json) = scope.promise {
        SavePairAssignmentDocumentCommand(
            TribeIdPairAssignmentDocument(
                TribeId(tribeId),
                pairAssignmentDocument.toPairAssignmentDocument()
            )
        )
            .perform()
            .document
            .toJson()
    }
}

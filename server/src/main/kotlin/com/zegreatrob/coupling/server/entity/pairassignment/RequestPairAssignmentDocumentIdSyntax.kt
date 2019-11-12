package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.server.external.express.Request

interface RequestPairAssignmentDocumentIdSyntax {
    fun Request.pairAssignmentDocumentId() = params["id"].toString().let(::PairAssignmentDocumentId)
}

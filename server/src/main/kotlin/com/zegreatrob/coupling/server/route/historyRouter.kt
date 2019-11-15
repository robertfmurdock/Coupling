package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.external.express.Router

val historyRouter = Router(routerParams(mergeParams = true)).apply {
    route("")
        .get(handleRequest { performPairAssignmentDocumentListQuery })
        .post(handleRequest { performSavePairAssignmentDocumentCommand })
    route("/:id")
        .delete(handleRequest { performDeletePairAssignmentDocumentCommand })
}
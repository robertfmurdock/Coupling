package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.external.express.Router

val historyRouter = Router(routerParams(mergeParams = true)).apply {
    route("")
        .post(dispatch { performSavePairAssignmentDocumentCommand })
    route("/:id")
        .delete(dispatch { performDeletePairAssignmentDocumentCommand })
}
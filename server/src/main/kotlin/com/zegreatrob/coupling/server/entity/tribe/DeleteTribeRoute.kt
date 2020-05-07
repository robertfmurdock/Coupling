package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.server.action.DeleteTribeCommand
import com.zegreatrob.coupling.server.express.ResponseHelpers.sendDeleteResults
import com.zegreatrob.coupling.server.express.route.dispatch
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.commandDispatcher
import com.zegreatrob.coupling.server.external.express.tribeId

val deleteTribeRoute = dispatch(::commandDispatcher, ::deleteTribeCommand, { it }, sendDeleteResults("Tribe"))

private fun deleteTribeCommand(request: Request) = with(request) { DeleteTribeCommand(tribeId()) }

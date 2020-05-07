package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.server.action.DeleteTribeCommand
import com.zegreatrob.coupling.server.express.route.ExpressDispatchers.command
import com.zegreatrob.coupling.server.express.route.dispatch
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.tribeId

val deleteTribeRoute = dispatch(command, ::deleteTribeCommand)

private fun deleteTribeCommand(request: Request) = with(request) { DeleteTribeCommand(tribeId()) }

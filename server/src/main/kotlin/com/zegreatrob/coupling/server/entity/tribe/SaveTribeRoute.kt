package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.server.action.tribe.SaveTribeCommand
import com.zegreatrob.coupling.server.express.ResponseHelpers.returnErrorOnFailure
import com.zegreatrob.coupling.server.express.route.ExpressDispatchers.command
import com.zegreatrob.coupling.server.express.route.dispatch
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.jsonBody

val saveTribeRoute = dispatch(command, Request::saveTribeCommand, { it }, ::returnErrorOnFailure)

private fun Request.saveTribeCommand() = SaveTribeCommand(jsonBody().toTribe())


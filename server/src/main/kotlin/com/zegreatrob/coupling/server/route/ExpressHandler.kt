package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response

typealias ExpressHandler = (Request, Response) -> Unit
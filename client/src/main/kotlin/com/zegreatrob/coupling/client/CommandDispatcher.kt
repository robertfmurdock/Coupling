package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.sdk.CouplingSdk
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax

class CommandDispatcher(private val sdk: CouplingSdk) :
    SuspendActionExecuteSyntax,
    CouplingSdk by sdk

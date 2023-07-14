package com.zegreatrob.coupling.client

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.TraceIdProvider
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.testmints.action.ActionCannon

class CommandDispatcher(override val traceId: Uuid, val sdk: ActionCannon<CouplingSdkDispatcher>) : TraceIdProvider

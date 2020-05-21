package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.actionFunc.Action

interface DispatchableAction<in T, R> : Action

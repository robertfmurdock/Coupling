package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.actionFunc.SimpleSuspendAction
import com.zegreatrob.coupling.actionFunc.SuspendAction

typealias SuspendResultAction<T, R> = SuspendAction<T, Result<R>>

typealias SimpleSuspendResultAction<T, R> = SimpleSuspendAction<T, Result<R>>

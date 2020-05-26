package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.actionFunc.async.SimpleSuspendAction
import com.zegreatrob.coupling.actionFunc.async.SuspendAction

typealias SuspendResultAction<T, R> = SuspendAction<T, Result<R>>

typealias SimpleSuspendResultAction<T, R> = SimpleSuspendAction<T, Result<R>>

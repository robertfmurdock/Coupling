package com.zegreatrob.coupling.action

import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import com.zegreatrob.testmints.action.async.SuspendAction

typealias SuspendResultAction<T, R> = SuspendAction<T, Result<R>>

typealias SimpleSuspendResultAction<T, R> = SimpleSuspendAction<T, Result<R>>

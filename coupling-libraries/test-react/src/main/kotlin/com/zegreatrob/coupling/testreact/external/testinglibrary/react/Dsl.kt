package com.zegreatrob.coupling.testreact.external.testinglibrary.react

import kotlinx.coroutines.await
import react.ReactNode

suspend fun <T : Any> waitFor(callback: () -> T?): Unit = testingLibraryReact.waitFor(callback).await()

val screen: Screen = testingLibraryReact.screen
fun render(node: ReactNode): Result = testingLibraryReact.render(node)
val render = testingLibraryReact::render
val fireEvent: FireEvent = testingLibraryReact.fireEvent
val act = testingLibraryReact::act
val within = testingLibraryReact::within

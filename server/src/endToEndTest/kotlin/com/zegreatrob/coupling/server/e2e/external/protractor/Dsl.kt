package com.zegreatrob.coupling.server.e2e.external.protractor

import kotlinx.coroutines.await

val waitToBePresentDuration = 5000

suspend fun ElementSelector.waitToBePresent() = browser.wait({ this.isPresent() }, waitToBePresentDuration, "").await()

suspend fun ElementSelector.performClick() = click().await()

suspend fun ElementSelector.performClear() = clear().await()

suspend fun ElementSelector.performSendKeys(value: String) = sendKeys(value).await()

suspend fun ElementSelector.performClearSendKeys(value: String) {
    performClear()
    performSendKeys(value)
}
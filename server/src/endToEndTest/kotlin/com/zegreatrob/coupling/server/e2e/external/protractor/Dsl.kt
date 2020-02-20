package com.zegreatrob.coupling.server.e2e.external.protractor

import kotlinx.coroutines.await

suspend fun ElementSelector.waitToBePresent() = browser.wait({ this.isPresent() }, 2000).await()
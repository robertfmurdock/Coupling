package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.external.setupBrowser
import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.browser
import kotlinx.coroutines.await

private val testingBrowser = setupBrowser(browser)

object ConfigForm : BrowserSyntax {
    val saveButton get() = WebdriverElement(finder = { testingBrowser.findByText("Save").await() })
    val deleteButton get() = WebdriverElement(finder = { testingBrowser.findByText("Retire").await() })
}

package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object AboutPage {
    suspend fun goTo() {
        WebdriverBrowser.setLocation("about")
        header().waitToExist()
    }

    suspend fun header() = TestingLibraryBrowser.findByText("About This App")
}

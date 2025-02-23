package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.wrapper.wdio.testing.library.RoleOptions
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object ConfigForm : BrowserSyntax {
    suspend fun saveButton() = TestingLibraryBrowser.getByRole("button", RoleOptions("Save"))
    suspend fun retireButton() = TestingLibraryBrowser.getByRole("button", RoleOptions("Retire"))
}

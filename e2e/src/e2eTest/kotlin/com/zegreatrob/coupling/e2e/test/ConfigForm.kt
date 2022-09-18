package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.wrapper.wdio.testing.library.RoleOptions
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object ConfigForm : BrowserSyntax {
    suspend fun getSaveButton() = TestingLibraryBrowser.getByRole("button", RoleOptions("Save"))
    suspend fun getDeleteButton() = TestingLibraryBrowser.getByRole("button", RoleOptions("Retire"))
}

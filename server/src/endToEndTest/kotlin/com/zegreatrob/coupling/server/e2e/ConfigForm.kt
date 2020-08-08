package com.zegreatrob.coupling.server.e2e

object ConfigForm : StyleSyntax {
    override val styles = loadStyles("ConfigForm")
    suspend fun getSaveButton() = getting("saveButton")
    suspend fun getDeleteButton() = getting("deleteButton")
}

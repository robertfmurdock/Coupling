package com.zegreatrob.coupling.cli

actual fun openBrowser(uri: String) {
    java.awt.Desktop.getDesktop().browse(java.net.URI(uri))
}

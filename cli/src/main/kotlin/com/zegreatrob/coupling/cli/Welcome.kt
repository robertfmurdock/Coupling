package com.zegreatrob.coupling.cli

import com.github.ajalt.clikt.core.CliktCommand

class Welcome : CliktCommand() {

    override fun run() {
        echo("Welcome to Coupling CLI.")
    }
}

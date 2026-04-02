package com.zegreatrob.coupling.cli.party

import node.fs.writeFileSync

actual fun writeFile(path: String, content: String) {
    writeFileSync(path, content)
}

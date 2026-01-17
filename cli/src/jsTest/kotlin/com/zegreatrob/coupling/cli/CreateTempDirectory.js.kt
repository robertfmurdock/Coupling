package com.zegreatrob.coupling.cli

import node.fs.mkdtempSync
import node.os.tmpdir
import node.path.path

actual fun createTempDirectory(): String = mkdtempSync(path.join(tmpdir(), "/coupling")).unsafeCast<String>()

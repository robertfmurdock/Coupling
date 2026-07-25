@file:JsModule("read-pkg-up")

package com.zegreatrob.coupling.cdnLookup.external.readpkgup

import kotlin.js.Json
import kotlin.js.Promise

external fun readPackageUp(options: Json): Promise<Json?>

package com.zegreatrob.coupling.client.external.corejs

@JsModule("core-js/features/object/assign")
external fun <T, R : T> objectAssign(dest: R, vararg src: T): R

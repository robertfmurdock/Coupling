@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "EXTERNAL_DELEGATION",
    "NESTED_CLASS_IN_EXTERNAL_INTERFACE",
)

package com.zegreatrob.coupling.client.components.external.blueimp.md5

@JsModule("blueimp-md5")
external fun md5(
    value: String,
    key: String? = definedExternally,
    raw: Boolean? = definedExternally,
): String = definedExternally

@JsModule("passport-custom")
@JsNonModule
external val passportCustom: dynamic

val CustomStrategy = passportCustom.Strategy

@JsModule("passport-azure-ad")
@JsNonModule
external val passportAzureAd: dynamic
val oidcStrategy = passportAzureAd.OIDCStrategy

@JsModule("passport-local")
@JsNonModule
external val passportLocal: dynamic

val localStrategy = passportLocal.Strategy
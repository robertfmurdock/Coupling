export default {
  "identityMetadata": "https://login.microsoftonline.com/common/v2.0/.well-known/openid-configuration",
  "clientID": "127e78ae-6b6a-4213-a83f-a644e2d1bb84",
  "responseType": "code id_token",
  "responseMode": "form_post",
  "redirectUrl": "http://localhost:3000/auth/signin-microsoft",
  "allowHttpForRedirectUrl": true,
  "clientSecret": "bhyrMCFDF9485=aoiJD1*%{",
  "validateIssuer": false,
  "issuer": null,
  "passReqToCallback": false,
  "useCookieInsteadOfSession": true,
  "cookieEncryptionKeys": [
    {
      "key": "12345678901234567890123456789012",
      "iv": "123456789012"
    },
    {
      "key": "abcdefghijklmnopqrstuvwxyzabcdef",
      "iv": "abcdefghijkl"
    }
  ],
  "scope": [
    "email",
    "profile"
  ],
  "loggingLevel": "warn",
  "nonceLifetime": null,
  "nonceMaxAmount": 5,
  "clockSkew": null
}
"use strict";
const Strategy = require('passport-custom').Strategy;
const OIDCStrategy = require('passport-azure-ad').OIDCStrategy;
const {OAuth2Client} = require('google-auth-library');
const passport = require('passport');
const LocalStrategy = require('passport-local').Strategy;
const config = require('./config');

function azureODICStrategy(userDataService) {
  return new OIDCStrategy({
      identityMetadata: config.microsoft.identityMetadata,
      clientID: config.microsoft.clientID,
      responseType: config.microsoft.responseType,
      responseMode: config.microsoft.responseMode,
      redirectUrl: config.microsoft.redirectUrl,
      allowHttpForRedirectUrl: config.microsoft.allowHttpForRedirectUrl,
      clientSecret: config.microsoft.clientSecret,
      validateIssuer: config.microsoft.validateIssuer,
      isB2C: config.microsoft.isB2C,
      issuer: config.microsoft.issuer,
      passReqToCallback: config.microsoft.passReqToCallback,
      scope: config.microsoft.scope,
      loggingLevel: config.microsoft.loggingLevel,
      nonceLifetime: config.microsoft.nonceLifetime,
      nonceMaxAmount: config.microsoft.nonceMaxAmount,
      useCookieInsteadOfSession: config.microsoft.useCookieInsteadOfSession,
      cookieEncryptionKeys: config.microsoft.cookieEncryptionKeys,
      clockSkew: config.microsoft.clockSkew,
    },
    function (iss, sub, profile, accessToken, refreshToken, done) {
      let email = profile._json.email;

      if (email) {
        userDataService.findOrCreate(email, null, function (err, user) {
          done(err, user);
        });
      } else {
        return done(new Error("Auth succeeded but no email found."), null);
      }
    }
  );
}

function googleAuthTransferStrategy(userDataService) {
  const clientID = config.googleClientID;
  const client = new OAuth2Client(clientID);

  async function verify(token) {
    const ticket = await client.verifyIdToken({
      idToken: token,
      audience: clientID
    });
    return ticket.getPayload();
  }

  return new Strategy(function (request, done) {
    verify(request.body.idToken)
      .then(payload => {
        userDataService.findOrCreate(payload.email, request.traceId, function (err, user) {
          done(err, user);
        });
      }, err => done(err))
  });
}

module.exports = function (app, userDataService) {
  const isInDevelopmentMode = 'development' == app.get('env') || 'test' == app.get('env');

  passport.use(googleAuthTransferStrategy(userDataService));
  passport.use(azureODICStrategy(userDataService));

  if (isInDevelopmentMode) {
    passport.use(new LocalStrategy(function (username, password, done) {
      userDataService.findOrCreate(username + "._temp", null, function (err, user) {
        if (err)
          console.error(`Problem with find or create user '${username}' in local strategy`, err);
        done(err, user);
      });
    }));
  }
};
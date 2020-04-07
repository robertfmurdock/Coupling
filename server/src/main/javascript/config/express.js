"use strict";
const logging = require("Coupling-logging");
const server = require("Coupling-server");
const compression = require('compression');
const path = require('path');
const favicon = require('serve-favicon');
const methodOverride = require('method-override');
const express = require('express');
const bodyParser = require('body-parser');
const Strategy = require('passport-custom').Strategy;
const OIDCStrategy = require('passport-azure-ad').OIDCStrategy;
const {OAuth2Client} = require('google-auth-library');
const errorHandler = require('errorhandler');
const cookieParser = require('cookie-parser');
const session = require('express-session');
const MongoStore = require('connect-mongo')(session);
const DynamoDBStore = require('connect-dynamodb')(session);
const passport = require('passport');
const LocalStrategy = require('passport-local').Strategy;
const statsd = require('express-statsd');
const config = require('./config');
const onFinished = require('on-finished');
const AWS = require('aws-sdk');

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
  app.use(compression());
  app.use(statsd({host: 'statsd', port: 8125}));

  app.set('port', config.port);
  app.set('views', [
    path.join(__dirname, 'public'),
    path.join(__dirname, 'views')
  ]);
  app.set('view engine', 'pug');
  app.use(favicon(path.join(__dirname, 'public/images/favicon.ico')));
  if (!process.env['DISABLE_LOGGING']) {
    app.use(function (request, response, next) {
      // noinspection JSUnresolvedVariable, JSUnresolvedFunction
      server.com.zegreatrob.coupling.server.logRequestAsync(request, response, function (callback) {
        onFinished(response, callback);
      });

      next();
    });
  }

  app.use(bodyParser.urlencoded({extended: true}));
  app.use(bodyParser.json());
  app.use(methodOverride());

  app.use(express.static(path.join(__dirname, 'public'), {extensions: ['json']}));
  app.use(cookieParser());

  let store;
  if (process.env.AWS_SECRET_ACCESS_KEY) {
    store = new DynamoDBStore({
      client: new AWS.DynamoDB({region: 'us-east-1'}),
    })
  } else if (process.env.LOCAL_DYNAMO) {
    store = new DynamoDBStore({
      client: new AWS.DynamoDB({region: 'us-east-1', endpoint: new AWS.Endpoint('http://localhost:8000')}),
    })
  } else {
    store = new MongoStore({url: config.mongoUrl})
  }

  app.use(session({
    secret: config.secret,
    resave: false,
    saveUninitialized: false,
    store: store
  }));
  app.use(passport.initialize());
  app.use(passport.session());

  app.use(function (err, req, res, next) {
    if (err) {
      req.logout();
      next(err);
    } else {
      next();
    }
  });

  const isInDevelopmentMode = 'development' == app.get('env') || 'test' == app.get('env');
  if (isInDevelopmentMode) {
    app.use(errorHandler());
  }

  // noinspection JSUnresolvedVariable, JSUnresolvedFunction
  logging.com.zegreatrob.coupling.logging.initializeJasmineLogging(isInDevelopmentMode);

  passport.serializeUser(userDataService.serializeUser);
  passport.deserializeUser(userDataService.deserializeUser);

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